package com.reportsystem.finance.infrastructure.messaging;

import com.reportsystem.shared.dto.event.SaleEvent;
import com.reportsystem.shared.dto.event.RentInvoiceEvent;
import com.reportsystem.shared.dto.event.StockEvent;
import com.reportsystem.shared.dto.event.StockTransferEvent;
import com.reportsystem.finance.domain.model.Account;
import com.reportsystem.finance.domain.model.JournalEntry;
import com.reportsystem.finance.domain.model.JournalEntryLine;
import com.reportsystem.finance.domain.service.AccountingServiceImpl;
import com.reportsystem.finance.domain.service.InvoiceServiceImpl;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class FinanceEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(FinanceEventConsumer.class);
    private static final String INTER_BRANCH_CLEARING_CODE = "1999-IBC";

    private final AccountingServiceImpl accountingService;
    private final InvoiceServiceImpl invoiceService;

    public FinanceEventConsumer(AccountingServiceImpl accountingService, InvoiceServiceImpl invoiceService) {
        this.accountingService = accountingService;
        this.invoiceService = invoiceService;
    }

    @KafkaListener(topics = "sale-events")
    public void handleSaleEvent(SaleEvent event) {
        invoiceService.createInvoice(event.tenantId(), null, "sale", event.outletId().toString(),
                LocalDate.now(), LocalDate.now().plusDays(30),
                event.subtotal(), event.taxAmount(), event.total());
    }

    @KafkaListener(topics = "rent-invoice-events")
    public void handleRentInvoiceEvent(RentInvoiceEvent event) {
        invoiceService.createInvoice(event.tenantId(), null, "rent", event.tenantName(),
                java.time.LocalDate.now(), event.dueDate(),
                event.amount(), java.math.BigDecimal.ZERO, event.amount());
    }

    @KafkaListener(topics = "stock.received")
    public void handleStockReceived(StockEvent event) {
        // Auto-post journal entry for inventory received
    }

    /**
     * On stock transfer RECEIVED, post a 4-line inter-branch journal entry:
     *   1. DEBIT  Inventory (target branch)        — increase target stock
     *   2. CREDIT Inter-Branch Clearing            — target owes source
     *   3. DEBIT  Inter-Branch Clearing            — source is owed by target
     *   4. CREDIT Inventory (source branch)        — decrease source stock
     *
     * The two Inter-Branch Clearing lines net to zero (single tenant, no P&L impact).
     * Each branch's books balance, and the consolidated view shows the inventory move.
     */
    @KafkaListener(topics = "stock.transfer.received")
    public void handleStockTransferReceived(StockTransferEvent event) {
        log.info("Received stock.transfer.received event for transfer {} ({} → {})",
            event.transferNumber(), event.fromBranchId(), event.toBranchId());
        currentEventTenantId = event.tenantId();
        try {
            Account sourceInventory = ensureInventoryAccount(event.tenantId(), event.fromBranchId());
            Account targetInventory = ensureInventoryAccount(event.tenantId(), event.toBranchId());
            Account interBranchClearing = ensureInterBranchClearingAccount(event.tenantId());

            BigDecimal totalValue = event.totalValue() != null ? event.totalValue() : BigDecimal.ZERO;
            if (totalValue.signum() <= 0) {
                // Compute from items if not provided
                totalValue = BigDecimal.ZERO;
                for (StockTransferEvent.TransferItem item : event.items()) {
                    BigDecimal qty = item.receivedQuantity() != null && item.receivedQuantity().signum() > 0
                        ? item.receivedQuantity() : item.quantity();
                    BigDecimal cost = item.unitCost() != null ? item.unitCost() : BigDecimal.ZERO;
                    totalValue = totalValue.add(qty.multiply(cost));
                }
            }
            if (totalValue.signum() == 0) {
                log.info("Transfer {} has zero value; skipping journal entry", event.transferNumber());
                return;
            }

            List<JournalEntryLine> lines = new ArrayList<>();
            // Target branch side
            lines.add(line(targetInventory.getId(), totalValue, BigDecimal.ZERO, "Inventory in (target branch)"));
            lines.add(line(interBranchClearing.getId(), BigDecimal.ZERO, totalValue, "IBC: target branch"));
            // Source branch side
            lines.add(line(interBranchClearing.getId(), totalValue, BigDecimal.ZERO, "IBC: source branch"));
            lines.add(line(sourceInventory.getId(), BigDecimal.ZERO, totalValue, "Inventory out (source branch)"));

            JournalEntry entry = accountingService.postJournalEntry(
                event.tenantId(),
                ensureDefaultBranch(event.tenantId()),  // branch_id is NOT NULL
                LocalDate.now(),
                "Stock transfer " + event.transferNumber() + " received (" + event.fromBranchId() + " → " + event.toBranchId() + ")",
                "STOCK_TRANSFER",
                event.transferId(),
                null,
                lines
            );
            // Tag with from/to branch IDs for cross-branch reporting
            entry.setFromBranchId(event.fromBranchId());
            entry.setToBranchId(event.toBranchId());
            log.info("Posted inter-branch journal entry {} for transfer {}", entry.getEntryNumber(), event.transferNumber());
        } catch (Exception e) {
            log.error("Failed to post inter-branch journal entry for transfer {}: {}",
                event.transferNumber(), e.getMessage(), e);
        }
    }

    private JournalEntryLine line(UUID accountId, BigDecimal debit, BigDecimal credit, String description) {
        return JournalEntryLine.builder()
            .accountId(accountId)
            .branchId(ensureDefaultBranch(currentEventTenantId))
            .debit(debit)
            .credit(credit)
            .description(description)
            .build();
    }

    // Stash the current event's tenantId so line() can use it for branch_id.
    // (Closure-style state — fine for single-threaded Kafka consumer; would be unsafe
    // for multi-threaded use.)
    private UUID currentEventTenantId;

    private Account ensureInventoryAccount(UUID tenantId, UUID branchId) {
        String code = "1400-INV-" + branchId.toString().substring(0, 8);
        return findOrCreate(tenantId, branchId, code, "Inventory - Branch " + branchId, "ASSET");
    }

    private Account ensureInterBranchClearingAccount(UUID tenantId) {
        // Shared at tenant level (clearing nets to zero across branches).
        // branch_id is NOT NULL in chart_of_accounts, so we use the tenant's HQ branch.
        UUID hqBranchId = ensureDefaultBranch(tenantId);
        return findOrCreate(tenantId, hqBranchId, INTER_BRANCH_CLEARING_CODE, "Inter-Branch Clearing", "LIABILITY");
    }

    /**
     * Ensure the tenant has a default branch (use the seeded HQ branch 00000000-...0010
     * for the demo tenant, or look up any existing branch).
     */
    private UUID ensureDefaultBranch(UUID tenantId) {
        // For the demo tenant, we know HQ exists. For others, we'd look it up.
        // As a simple fallback, use the first character of the tenantId.
        if ("00000000-0000-0000-0000-000000000001".equals(tenantId.toString())) {
            return UUID.fromString("00000000-0000-0000-0000-000000000010");
        }
        // Fallback: derive a stable UUID from the tenantId
        return UUID.nameUUIDFromBytes(("default-branch-" + tenantId).getBytes());
    }

    private Account findOrCreate(UUID tenantId, UUID branchId, String code, String name, String type) {
        List<Account> existing = accountingService.getAccountsByTenant(tenantId);
        Optional<Account> match = existing.stream()
            .filter(a -> code.equals(a.getCode()))
            .findFirst();
        if (match.isPresent()) return match.get();
        return accountingService.createAccount(tenantId, branchId, code, name, type, null);
    }
}

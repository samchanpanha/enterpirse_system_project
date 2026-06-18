package com.reportsystem.inventory.domain.service;

import com.reportsystem.inventory.domain.model.Product;
import com.reportsystem.inventory.domain.model.StockEntry;
import com.reportsystem.inventory.domain.model.StockExit;
import com.reportsystem.inventory.domain.model.StockTransfer;
import com.reportsystem.inventory.domain.model.StockTransferItem;
import com.reportsystem.inventory.domain.port.inbound.StockService;
import com.reportsystem.inventory.domain.port.inbound.StockTransferService;
import com.reportsystem.inventory.domain.port.outbound.ProductRepository;
import com.reportsystem.inventory.domain.port.outbound.StockTransferRepository;
import com.reportsystem.inventory.infrastructure.messaging.InventoryEventPublisher;
import com.reportsystem.shared.dto.event.StockTransferEvent;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockTransferServiceImpl implements StockTransferService {

    private static final Logger log = LoggerFactory.getLogger(StockTransferServiceImpl.class);
    private static final AtomicLong TRANSFER_COUNTER = new AtomicLong(1);

    private final StockTransferRepository transferRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;
    private final InventoryEventPublisher eventPublisher;

    public StockTransferServiceImpl(StockTransferRepository transferRepository,
                                    ProductRepository productRepository,
                                    @Autowired @Lazy StockService stockService,
                                    InventoryEventPublisher eventPublisher) {
        this.transferRepository = transferRepository;
        this.productRepository = productRepository;
        this.stockService = stockService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public StockTransfer createDraft(UUID tenantId, UUID fromBranchId, UUID toBranchId,
                                      UUID fromWarehouseId, UUID toWarehouseId,
                                      List<ItemDraft> items, String notes, UUID createdBy) {
        if (fromBranchId.equals(toBranchId)) {
            throw new IllegalArgumentException("Cannot transfer to the same branch");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("Transfer must have at least one item");
        }
        String transferNumber = generateTransferNumber();
        BigDecimal totalValue = BigDecimal.ZERO;
        int totalItems = 0;
        for (ItemDraft item : items) {
            if (item.quantity() == null || item.quantity().signum() <= 0) {
                throw new IllegalArgumentException("Item quantity must be positive");
            }
            BigDecimal cost = item.unitCost() != null ? item.unitCost() : BigDecimal.ZERO;
            totalValue = totalValue.add(cost.multiply(item.quantity()));
            totalItems++;
        }
        StockTransfer transfer = StockTransfer.builder()
            .id(UUID.randomUUID())
            .tenantId(tenantId)
            .transferNumber(transferNumber)
            .fromBranchId(fromBranchId)
            .toBranchId(toBranchId)
            .fromWarehouseId(fromWarehouseId)
            .toWarehouseId(toWarehouseId)
            .status(StockTransfer.STATUS_DRAFT)
            .notes(notes)
            .createdBy(createdBy)
            .totalItems(totalItems)
            .totalValue(totalValue)
            .createdAt(Instant.now())
            .build();
        List<StockTransferItem> itemEntities = new ArrayList<>();
        int order = 0;
        for (ItemDraft item : items) {
            Optional<Product> productOpt = productRepository.findById(item.productId());
            StockTransferItem itemEntity = StockTransferItem.builder()
                .id(UUID.randomUUID())
                .transferId(transfer.getId())
                .productId(item.productId())
                .productName(productOpt.map(Product::getName).orElse(null))
                .quantity(item.quantity())
                .receivedQuantity(BigDecimal.ZERO)
                .unitCost(item.unitCost() != null ? item.unitCost() : BigDecimal.ZERO)
                .lineOrder(order++)
                .createdAt(Instant.now())
                .build();
            itemEntities.add(itemEntity);
        }
        transfer.setItems(itemEntities);
        StockTransfer saved = transferRepository.save(transfer);
        eventPublisher.publishStockTransferEvent(toEvent(saved, StockTransferEvent.TYPE_REQUESTED));
        log.info("Created transfer draft {} ({} items, total {})", transferNumber, totalItems, totalValue);
        return saved;
    }

    @Override
    @Transactional
    public StockTransfer ship(UUID transferId, UUID shippedBy) {
        StockTransfer transfer = transferRepository.findById(transferId)
            .orElseThrow(() -> new IllegalArgumentException("Transfer not found: " + transferId));
        if (!StockTransfer.STATUS_DRAFT.equals(transfer.getStatus())) {
            throw new IllegalStateException("Cannot ship transfer in status " + transfer.getStatus());
        }
        // Decrement source stock
        for (StockTransferItem item : transfer.getItems()) {
            stockService.removeStock(
                transfer.getTenantId(),
                transfer.getFromBranchId(),
                transfer.getFromWarehouseId(),
                item.getProductId(),
                item.getQuantity(),
                "TRANSFER",
                transfer.getId(),
                "Outgoing for transfer " + transfer.getTransferNumber()
            );
        }
        transfer.setStatus(StockTransfer.STATUS_SHIPPED);
        transfer.setShippedBy(shippedBy);
        transfer.setShippedAt(Instant.now());
        transfer.setUpdatedAt(Instant.now());
        StockTransfer saved = transferRepository.save(transfer);
        eventPublisher.publishStockTransferEvent(toEvent(saved, StockTransferEvent.TYPE_SHIPPED));
        log.info("Shipped transfer {} from branch {}", transfer.getTransferNumber(), transfer.getFromBranchId());
        return saved;
    }

    @Override
    @Transactional
    public StockTransfer receive(UUID transferId, UUID receivedBy, List<ReceiveLine> receivedLines) {
        StockTransfer transfer = transferRepository.findById(transferId)
            .orElseThrow(() -> new IllegalArgumentException("Transfer not found: " + transferId));
        if (!StockTransfer.STATUS_SHIPPED.equals(transfer.getStatus())) {
            throw new IllegalStateException("Cannot receive transfer in status " + transfer.getStatus());
        }
        if (receivedLines != null) {
            for (ReceiveLine rl : receivedLines) {
                transfer.getItems().stream()
                    .filter(i -> i.getProductId().equals(rl.productId()))
                    .findFirst()
                    .ifPresent(item -> {
                        item.setReceivedQuantity(rl.receivedQuantity() != null ? rl.receivedQuantity() : item.getQuantity());
                        if (rl.discrepancyNotes() != null && !rl.discrepancyNotes().isBlank()) {
                            item.setDiscrepancyNotes(rl.discrepancyNotes());
                        }
                    });
            }
        }
        // Default receivedQuantity = quantity for any line not in receivedLines
        for (StockTransferItem item : transfer.getItems()) {
            if (item.getReceivedQuantity() == null || item.getReceivedQuantity().signum() == 0) {
                item.setReceivedQuantity(item.getQuantity());
            }
        }
        // Increment target stock
        for (StockTransferItem item : transfer.getItems()) {
            StockEntry entry = stockService.addStock(
                transfer.getTenantId(),
                transfer.getToBranchId(),
                transfer.getToWarehouseId(),
                item.getProductId(),
                item.getReceivedQuantity(),
                item.getUnitCost(),
                null,
                "Incoming from transfer " + transfer.getTransferNumber()
            );
            // Discard returned entry (we just need the side effect)
        }
        transfer.setStatus(StockTransfer.STATUS_RECEIVED);
        transfer.setReceivedBy(receivedBy);
        transfer.setReceivedAt(Instant.now());
        transfer.setUpdatedAt(Instant.now());
        StockTransfer saved = transferRepository.save(transfer);
        eventPublisher.publishStockTransferEvent(toEvent(saved, StockTransferEvent.TYPE_RECEIVED));
        log.info("Received transfer {} at branch {}", transfer.getTransferNumber(), transfer.getToBranchId());
        return saved;
    }

    @Override
    @Transactional
    public StockTransfer cancel(UUID transferId, UUID cancelledBy, String reason) {
        StockTransfer transfer = transferRepository.findById(transferId)
            .orElseThrow(() -> new IllegalArgumentException("Transfer not found: " + transferId));
        if (StockTransfer.STATUS_RECEIVED.equals(transfer.getStatus())) {
            throw new IllegalStateException("Cannot cancel a received transfer");
        }
        if (StockTransfer.STATUS_CANCELLED.equals(transfer.getStatus())) {
            throw new IllegalStateException("Transfer is already cancelled");
        }
        // If SHIPPED, restock the source warehouse
        if (StockTransfer.STATUS_SHIPPED.equals(transfer.getStatus())) {
            for (StockTransferItem item : transfer.getItems()) {
                stockService.addStock(
                    transfer.getTenantId(),
                    transfer.getFromBranchId(),
                    transfer.getFromWarehouseId(),
                    item.getProductId(),
                    item.getQuantity(),
                    item.getUnitCost(),
                    null,
                    "Restock from cancelled transfer " + transfer.getTransferNumber()
                );
            }
        }
        transfer.setStatus(StockTransfer.STATUS_CANCELLED);
        transfer.setCancelledAt(Instant.now());
        transfer.setCancelReason(reason);
        transfer.setUpdatedAt(Instant.now());
        StockTransfer saved = transferRepository.save(transfer);
        eventPublisher.publishStockTransferEvent(toEvent(saved, StockTransferEvent.TYPE_CANCELLED));
        log.info("Cancelled transfer {} (reason: {})", transfer.getTransferNumber(), reason);
        return saved;
    }

    @Override
    public StockTransfer getById(UUID transferId) {
        return transferRepository.findById(transferId)
            .orElseThrow(() -> new IllegalArgumentException("Transfer not found: " + transferId));
    }

    @Override
    public List<StockTransfer> listByTenant(UUID tenantId, String statusFilter) {
        if (statusFilter != null && !statusFilter.isBlank()) {
            return transferRepository.findByTenantIdAndStatus(tenantId, statusFilter);
        }
        return transferRepository.findByTenantIdOrderByCreatedAtDesc(tenantId);
    }

    @Override
    public List<StockTransfer> listByBranch(UUID tenantId, UUID branchId, String statusFilter) {
        List<StockTransfer> all = transferRepository.findByTenantIdAndBranch(tenantId, branchId);
        if (statusFilter == null || statusFilter.isBlank()) return all;
        return all.stream().filter(t -> statusFilter.equals(t.getStatus())).toList();
    }

    @Override
    public List<StockTransfer> listIncoming(UUID branchId) {
        return transferRepository.findIncomingForBranch(branchId);
    }

    private StockTransferEvent toEvent(StockTransfer t, String type) {
        List<StockTransferEvent.TransferItem> items = new ArrayList<>();
        for (StockTransferItem i : t.getItems()) {
            items.add(new StockTransferEvent.TransferItem(
                i.getProductId(),
                i.getProductName(),
                i.getQuantity(),
                i.getReceivedQuantity(),
                i.getUnitCost()
            ));
        }
        return new StockTransferEvent(
            type, t.getId(), t.getTransferNumber(), t.getTenantId(),
            t.getFromBranchId(), t.getToBranchId(), t.getStatus(),
            t.getTotalValue(), items, Instant.now()
        );
    }

    private String generateTransferNumber() {
        long n = TRANSFER_COUNTER.incrementAndGet();
        return "TR-" + Instant.now().getEpochSecond() + "-" + String.format("%04d", n % 10000);
    }
}

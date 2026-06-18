package com.reportsystem.inventory.domain.port.inbound;

import com.reportsystem.inventory.domain.model.StockTransfer;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface StockTransferService {
    /**
     * Create a new draft transfer. Status: DRAFT.
     * The transfer must have at least one item.
     */
    StockTransfer createDraft(UUID tenantId, UUID fromBranchId, UUID toBranchId,
                               UUID fromWarehouseId, UUID toWarehouseId,
                               List<ItemDraft> items, String notes, UUID createdBy);

    /**
     * Ship a draft transfer: decrements source stock for each item.
     * Status: DRAFT → SHIPPED. Publishes stock.transfer.shipped event.
     */
    StockTransfer ship(UUID transferId, UUID shippedBy);

    /**
     * Receive a shipped transfer: increments target stock for each item.
     * Status: SHIPPED → RECEIVED. Publishes stock.transfer.received event.
     * If any line has a discrepancy (receivedQuantity != quantity), it's recorded.
     */
    StockTransfer receive(UUID transferId, UUID receivedBy, List<ReceiveLine> receivedLines);

    /**
     * Cancel a draft or shipped transfer.
     * If SHIPPED, restocks the source warehouse.
     * Status: → CANCELLED. Publishes stock.transfer.cancelled event.
     */
    StockTransfer cancel(UUID transferId, UUID cancelledBy, String reason);

    /**
     * Get a transfer with all items.
     */
    StockTransfer getById(UUID transferId);

    /**
     * List transfers for a tenant (all branches).
     */
    List<StockTransfer> listByTenant(UUID tenantId, String statusFilter);

    /**
     * List transfers where the given branch is either source or destination.
     */
    List<StockTransfer> listByBranch(UUID tenantId, UUID branchId, String statusFilter);

    /**
     * List incoming transfers (status=SHIPPED) for a branch — i.e., the receiving
     * branch's pending inbox.
     */
    List<StockTransfer> listIncoming(UUID branchId);

    record ItemDraft(UUID productId, BigDecimal quantity, BigDecimal unitCost) {}

    record ReceiveLine(UUID productId, BigDecimal receivedQuantity, String discrepancyNotes) {}
}

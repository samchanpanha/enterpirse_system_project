package com.reportsystem.inventory.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransfer {
    private UUID id;
    private UUID tenantId;
    private String transferNumber;
    private UUID fromBranchId;
    private UUID toBranchId;
    private UUID fromWarehouseId;
    private UUID toWarehouseId;
    private String status;            // DRAFT, SHIPPED, RECEIVED, CANCELLED
    private String notes;
    private UUID createdBy;
    private UUID approvedBy;
    private UUID shippedBy;
    private UUID receivedBy;
    private Instant shippedAt;
    private Instant receivedAt;
    private Instant cancelledAt;
    private String cancelReason;
    private int totalItems;
    private BigDecimal totalValue;
    private Instant createdAt;
    private Instant updatedAt;
    private List<StockTransferItem> items;

    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_SHIPPED = "SHIPPED";
    public static final String STATUS_RECEIVED = "RECEIVED";
    public static final String STATUS_CANCELLED = "CANCELLED";
}

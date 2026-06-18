package com.reportsystem.inventory.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stock_transfers")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class StockTransferEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "transfer_number", nullable = false, length = 30) private String transferNumber;
    @Column(name = "from_branch_id", nullable = false) private UUID fromBranchId;
    @Column(name = "to_branch_id", nullable = false) private UUID toBranchId;
    @Column(name = "from_warehouse_id") private UUID fromWarehouseId;
    @Column(name = "to_warehouse_id") private UUID toWarehouseId;
    @Column(nullable = false, length = 30) private String status;
    @Column(columnDefinition = "TEXT") private String notes;
    @Column(name = "created_by") private UUID createdBy;
    @Column(name = "approved_by") private UUID approvedBy;
    @Column(name = "shipped_by") private UUID shippedBy;
    @Column(name = "received_by") private UUID receivedBy;
    @Column(name = "shipped_at") private Instant shippedAt;
    @Column(name = "received_at") private Instant receivedAt;
    @Column(name = "cancelled_at") private Instant cancelledAt;
    @Column(name = "cancel_reason", columnDefinition = "TEXT") private String cancelReason;
    @Column(name = "total_items", nullable = false) private int totalItems;
    @Column(name = "total_value", nullable = false, precision = 18, scale = 2) private BigDecimal totalValue;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at") private Instant updatedAt;

    // Items are persisted separately by the adapter (FK constraint requires parent to exist first).
    // No @OneToMany here to avoid JPA trying to insert items in the wrong order.
}

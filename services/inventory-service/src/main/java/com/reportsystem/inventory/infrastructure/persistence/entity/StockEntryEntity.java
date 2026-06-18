package com.reportsystem.inventory.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;


@Entity
@Table(name = "stock_entries")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class StockEntryEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "warehouse_id", nullable = false) private UUID warehouseId;
    @Column(name = "product_id", nullable = false) private UUID productId;
    private java.math.BigDecimal quantity; @Column(name = "unit_cost") private java.math.BigDecimal unitCost;
    @Column(name = "batch_number") private String batchNumber; @Column(name = "expiry_date") private java.time.LocalDate expiryDate;
    @Column(name = "reference_type") private String referenceType; @Column(name = "reference_id") private UUID referenceId;
    private String notes; @Column(name = "created_by") private UUID createdBy;
    @Column(name = "branch_id", nullable = false) private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
}
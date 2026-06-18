package com.reportsystem.inventory.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;


@Entity
@Table(name = "purchase_orders")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PurchaseOrderEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "supplier_id", nullable = false) private UUID supplierId;
    @Column(name = "po_number") private String poNumber;
    private String status; @Column(name = "order_date") private java.time.LocalDate orderDate;
    @Column(name = "expected_date") private java.time.LocalDate expectedDate;
    @Column(name = "received_date") private java.time.LocalDate receivedDate;
    private java.math.BigDecimal subtotal; @Column(name = "tax_amount") private java.math.BigDecimal taxAmount;
    @Column(name = "shipping_cost") private java.math.BigDecimal shippingCost;
    private java.math.BigDecimal total; private String currency;
    private String notes; @Column(name = "created_by") private UUID createdBy;
    @Column(name = "approved_by") private UUID approvedBy;
    @Column(name = "branch_id", nullable = false) private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at") private Instant updatedAt;
}
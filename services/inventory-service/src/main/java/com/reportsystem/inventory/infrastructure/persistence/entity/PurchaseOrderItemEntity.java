package com.reportsystem.inventory.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;


@Entity
@Table(name = "purchase_order_items")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PurchaseOrderItemEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "po_id", nullable = false) private UUID purchaseOrderId;
    @Column(name = "product_id", nullable = false) private UUID productId;
    @Column(name = "quantity_ordered") private java.math.BigDecimal quantityOrdered;
    @Column(name = "quantity_received") private java.math.BigDecimal quantityReceived;
    @Column(name = "unit_cost") private java.math.BigDecimal unitCost;
    private java.math.BigDecimal subtotal;
    @Column(name = "branch_id", nullable = false) private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
}
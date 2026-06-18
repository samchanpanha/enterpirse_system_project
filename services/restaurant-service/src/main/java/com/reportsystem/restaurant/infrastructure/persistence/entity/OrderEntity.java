package com.reportsystem.restaurant.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity @Table(name = "orders")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "outlet_id", nullable = false)
    private UUID outletId;
    @Column(name = "table_id")
    private UUID tableId;
    @Column(name = "customer_id")
    private UUID customerId;
    @Column(name = "order_number", nullable = false)
    private String orderNumber;
    private String type;
    private String status;
    private BigDecimal subtotal;
    private BigDecimal discount;
    @Column(name = "tax_amount")
    private BigDecimal taxAmount;
    @Column(name = "service_charge")
    private BigDecimal serviceCharge;
    private BigDecimal total;
    @Column(name = "payment_status")
    private String paymentStatus;
    private String notes;
    @Column(name = "served_by")
    private UUID servedBy;
    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "completed_at")
    private Instant completedAt;
}

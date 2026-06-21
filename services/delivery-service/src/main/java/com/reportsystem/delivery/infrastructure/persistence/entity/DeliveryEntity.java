package com.reportsystem.delivery.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity @Table(name = "deliveries")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DeliveryEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "branch_id")
    private UUID branchId;
    @Column(name = "outlet_id")
    private UUID outletId;
    @Column(name = "order_id")
    private UUID orderId;
    @Column(name = "driver_id")
    private UUID driverId;
    @Column(name = "customer_name")
    private String customerName;
    @Column(name = "customer_phone")
    private String customerPhone;
    @Column(name = "delivery_address")
    private String deliveryAddress;
    @Column(name = "pickup_address")
    private String pickupAddress;
    private String status;
    @Column(name = "scheduled_at")
    private Instant scheduledAt;
    @Column(name = "picked_up_at")
    private Instant pickedUpAt;
    @Column(name = "delivered_at")
    private Instant deliveredAt;
    @Column(name = "delivery_fee")
    private BigDecimal deliveryFee;
    @Column(name = "distance_km")
    private BigDecimal distanceKm;
    private String notes;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb default '{}'")
    private String metadata;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
}

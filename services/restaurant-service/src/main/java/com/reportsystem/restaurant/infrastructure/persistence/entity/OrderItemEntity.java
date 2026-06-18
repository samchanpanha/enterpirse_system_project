package com.reportsystem.restaurant.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity @Table(name = "order_items")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class OrderItemEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "order_id", nullable = false)
    private UUID orderId;
    @Column(name = "menu_item_id", nullable = false)
    private UUID menuItemId;
    private int quantity;
    @Column(name = "unit_price")
    private BigDecimal unitPrice;
    @JdbcTypeCode(SqlTypes.JSON)

    @Column(columnDefinition = "jsonb default '{}'")
    private String modifiers;
    private BigDecimal subtotal;
    private String status;
    private String notes;
    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}

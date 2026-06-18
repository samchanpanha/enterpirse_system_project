package com.reportsystem.inventory.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "stock_transfer_items")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class StockTransferItemEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "transfer_id", nullable = false) private UUID transferId;
    @Column(name = "product_id", nullable = false) private UUID productId;
    @Column(nullable = false, precision = 18, scale = 4) private BigDecimal quantity;
    @Column(name = "received_quantity", nullable = false, precision = 18, scale = 4) private BigDecimal receivedQuantity;
    @Column(name = "unit_cost", nullable = false, precision = 18, scale = 4) private BigDecimal unitCost;
    @Column(name = "discrepancy_notes", columnDefinition = "TEXT") private String discrepancyNotes;
    @Column(name = "line_order", nullable = false) private int lineOrder;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
}

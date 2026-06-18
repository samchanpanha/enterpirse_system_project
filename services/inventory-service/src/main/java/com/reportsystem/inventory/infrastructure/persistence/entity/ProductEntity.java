package com.reportsystem.inventory.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;


@Entity
@Table(name = "products")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProductEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "category_id") private UUID categoryId;
    private String name; @Column(name = "name_kh") private String nameKh;
    private String sku; private String barcode; private String unit;
    @Column(name = "unit_price") private java.math.BigDecimal unitPrice;
    @Column(name = "cost_price") private java.math.BigDecimal costPrice;
    @Column(name = "min_stock") private java.math.BigDecimal minStock;
    @Column(name = "max_stock") private java.math.BigDecimal maxStock;
    @Column(name = "is_tracked") private boolean tracked;
    @Column(name = "is_active") private boolean active;
    @Column(name = "image_url") private String imageUrl;
    @Column(name = "branch_id", nullable = false) private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at") private Instant updatedAt;
}
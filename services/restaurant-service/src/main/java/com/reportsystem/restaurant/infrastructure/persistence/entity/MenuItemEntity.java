package com.reportsystem.restaurant.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity @Table(name = "menu_items")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class MenuItemEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "category_id", nullable = false)
    private UUID categoryId;
    @Column(nullable = false)
    private String name;
    @Column(name = "name_kh")
    private String nameKh;
    private String description;
    @Column(name = "description_kh")
    private String descriptionKh;
    @Column(nullable = false)
    private BigDecimal price;
    private String currency;
    @Column(name = "tax_rate")
    private BigDecimal taxRate;
    @Column(name = "image_url")
    private String imageUrl;
    @JdbcTypeCode(SqlTypes.JSON)

    @Column(columnDefinition = "jsonb default '{}'")
    private String options;
    @JdbcTypeCode(SqlTypes.JSON)

    @Column(columnDefinition = "jsonb default '{}'")
    private String modifiers;
    @Column(name = "is_active")
    private boolean active;
    @Column(name = "sort_order")
    private int sortOrder;
    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
}

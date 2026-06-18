package com.reportsystem.inventory.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;


@Entity
@Table(name = "product_categories")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ProductCategoryEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    private String name; @Column(name = "parent_id") private UUID parentId;
    @Column(name = "branch_id", nullable = false) private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
}
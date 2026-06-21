package com.reportsystem.realty.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity @Table(name = "agents")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AgentEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "user_id")
    private UUID userId;
    @Column(nullable = false)
    private String name;
    @Column(name = "name_kh")
    private String nameKh;
    private String phone;
    private String email;
    @Column(name = "license_number")
    private String licenseNumber;
    @Column(columnDefinition = "TEXT")
    private String bio;
    @Column(name = "avatar_url", columnDefinition = "TEXT")
    private String avatarUrl;
    @Column(precision = 3, scale = 2)
    private BigDecimal rating;
    @Column(name = "total_sales")
    private int totalSales;
    @Column(name = "total_listings")
    private int totalListings;
    @Column(name = "is_active")
    private boolean active;
    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
}

package com.reportsystem.delivery.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity @Table(name = "delivery_zones")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DeliveryZoneEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "branch_id")
    private UUID branchId;
    @Column(nullable = false)
    private String name;
    @Column(name = "name_kh")
    private String nameKh;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb default '{}'")
    private String boundaries;
    @Column(name = "base_fee")
    private BigDecimal baseFee;
    @Column(name = "per_km_fee")
    private BigDecimal perKmFee;
    @Column(name = "min_fee")
    private BigDecimal minFee;
    @Column(name = "max_fee")
    private BigDecimal maxFee;
    @Column(name = "estimated_minutes")
    private Integer estimatedMinutes;
    @Column(name = "is_active")
    private boolean isActive;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
}

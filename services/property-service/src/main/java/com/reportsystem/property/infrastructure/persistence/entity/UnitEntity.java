package com.reportsystem.property.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "units")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class UnitEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "property_id", nullable = false)
    private UUID propertyId;
    @Column(nullable = false)
    private String label;
    private Integer floor;
    private Integer bedrooms;
    private Integer bathrooms;
    @Column(name = "area_sqm")
    private BigDecimal areaSqm;
    @Column(name = "rent_amount")
    private BigDecimal rentAmount;
    @Column(name = "deposit_amount")
    private BigDecimal depositAmount;
    private String currency;
    private String status;
    private String type;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb default '[]'")
    private String amenities;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb default '[]'")
    private String images;
    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
}

package com.reportsystem.realty.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity @Table(name = "property_valuations")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PropertyValuationEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "property_id")
    private UUID propertyId;
    @Column(name = "estimated_value", precision = 15, scale = 2)
    private BigDecimal estimatedValue;
    @Column(name = "low_range", precision = 15, scale = 2)
    private BigDecimal lowRange;
    @Column(name = "high_range", precision = 15, scale = 2)
    private BigDecimal highRange;
    @Column(name = "confidence_score", precision = 5, scale = 2)
    private BigDecimal confidenceScore;
    @Column(name = "valuation_date")
    private LocalDate valuationDate;
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb default '{}'")
    private String factors;
    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}

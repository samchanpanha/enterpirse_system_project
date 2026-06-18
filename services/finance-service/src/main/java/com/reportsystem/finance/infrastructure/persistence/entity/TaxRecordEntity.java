package com.reportsystem.finance.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;


@Entity
@Table(name = "tax_records")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TaxRecordEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "tax_type") private String taxType;
    @Column(name = "period_month") private int periodMonth;
    @Column(name = "period_year") private int periodYear;
    @Column(name = "taxable_amount") private BigDecimal taxableAmount;
    @Column(name = "tax_rate") private BigDecimal taxRate;
    @Column(name = "tax_amount") private BigDecimal taxAmount;
    @Column(name = "source_type") private String sourceType;
    @Column(name = "source_id") private UUID sourceId;
    @Column(name = "branch_id", nullable = false) private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
}
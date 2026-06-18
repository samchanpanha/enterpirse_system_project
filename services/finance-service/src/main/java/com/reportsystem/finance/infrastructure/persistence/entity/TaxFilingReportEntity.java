package com.reportsystem.finance.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;


@Entity
@Table(name = "tax_filing_reports")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TaxFilingReportEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "tax_type") private String taxType;
    @Column(name = "period_month") private Integer periodMonth;
    @Column(name = "period_year") private int periodYear;
    @Column(name = "period_type") private String periodType;
    @Column(name = "total_tax") private BigDecimal totalTax;
    private String status;
    @Column(name = "filed_date") private LocalDate filedDate;
    @Column(name = "reference_number") private String referenceNumber;
    @Column(name = "export_format") private String exportFormat;
    @Column(name = "export_url") private String exportUrl;
    @Column(name = "branch_id", nullable = false) private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at") private Instant updatedAt;
}
package com.reportsystem.payment.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;


@Entity
@Table(name = "reconciliation_records")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ReconciliationRecordEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    private String gateway; @Column(name = "statement_date") private LocalDate statementDate;
    @Column(name = "total_expected") private BigDecimal totalExpected;
    @Column(name = "total_matched") private BigDecimal totalMatched;
    @Column(name = "total_unmatched") private BigDecimal totalUnmatched;
    private String status; @Column(name = "matched_count") private int matchedCount;
    @Column(name = "unmatched_count") private int unmatchedCount;
    @Column(name = "processed_at") private Instant processedAt;
    @Column(name = "branch_id", nullable = false) private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
}
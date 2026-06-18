package com.reportsystem.reporting.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;


@Entity
@Table(name = "report_executions")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ReportExecutionEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "report_id", nullable = false) private UUID reportId;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(columnDefinition = "jsonb") private String parameters;
    private String status; @Column(name = "output_url") private String outputUrl;
    @Column(name = "row_count") private Integer rowCount;
    @Column(name = "duration_ms") private Integer durationMs;
    @Column(name = "error_message") private String errorMessage;
    @Column(name = "requested_by") private UUID requestedBy;
    @Column(name = "started_at") private Instant startedAt;
    @Column(name = "completed_at") private Instant completedAt;
    @Column(name = "branch_id", nullable = false) private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
}
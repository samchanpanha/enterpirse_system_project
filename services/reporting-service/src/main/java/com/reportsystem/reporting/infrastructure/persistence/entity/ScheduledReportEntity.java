package com.reportsystem.reporting.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;


@Entity
@Table(name = "scheduled_reports")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ScheduledReportEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "report_id", nullable = false) private UUID reportId;
    @Column(name = "schedule_cron") private String scheduleCron;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb NOT NULL") private String recipients;
    private String format; @Column(name = "is_active") private boolean active;
    @Column(name = "last_sent_at") private Instant lastSentAt;
    @Column(name = "branch_id", nullable = false) private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
}
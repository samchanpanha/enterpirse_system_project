package com.reportsystem.property.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "schedules")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ScheduleEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "unit_id", nullable = false)
    private UUID unitId;
    @Column(nullable = false)
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    @Column(nullable = false)
    private String type;
    @Column(name = "interval_type", nullable = false)
    private String intervalType;
    @Column(name = "start_time", nullable = false)
    private Instant startTime;
    @Column(name = "end_time")
    private Instant endTime;
    @Column(name = "recurring_rule", columnDefinition = "jsonb")
    private String recurringRule;
    private String status;
    @Column(name = "created_by")
    private UUID createdBy;
    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
}

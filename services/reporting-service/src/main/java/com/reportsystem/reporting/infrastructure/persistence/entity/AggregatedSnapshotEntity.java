package com.reportsystem.reporting.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;


@Entity
@Table(name = "aggregated_snapshots")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AggregatedSnapshotEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "snapshot_type") private String snapshotType;
    @Column(name = "snapshot_date") private LocalDate snapshotDate;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb NOT NULL") private String data;
    @Column(name = "branch_id", nullable = false) private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
}
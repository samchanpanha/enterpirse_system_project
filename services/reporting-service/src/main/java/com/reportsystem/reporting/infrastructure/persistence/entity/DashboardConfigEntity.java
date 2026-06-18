package com.reportsystem.reporting.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;


@Entity
@Table(name = "dashboard_configs")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DashboardConfigEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    private String name; @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb NOT NULL") private String layout;
    @Column(name = "is_default") private boolean isDefault;
    @Column(name = "created_by") private UUID createdBy;
    @Column(name = "branch_id", nullable = false) private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at") private Instant updatedAt;
}
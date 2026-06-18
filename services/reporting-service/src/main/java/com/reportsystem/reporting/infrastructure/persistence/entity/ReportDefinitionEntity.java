package com.reportsystem.reporting.infrastructure.persistence.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;


@Entity
@Table(name = "report_definitions")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class ReportDefinitionEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    private String name; private String code; private String type;
    @JdbcTypeCode(SqlTypes.JSON) @Column(columnDefinition = "jsonb NOT NULL") private String config;
    @Column(name = "is_system") private boolean system;
    @Column(name = "branch_id", nullable = false) private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at") private Instant updatedAt;
}
package com.reportsystem.finance.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;


@Entity
@Table(name = "chart_of_accounts")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AccountEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    private String code; private String name; private String type;
    @Column(name = "is_active") private boolean active;
    @Column(name = "is_contra") private boolean contra;
    @Column(name = "parent_id") private UUID parentId;
    @Column(name = "branch_id", nullable = false) private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at") private Instant updatedAt;
}
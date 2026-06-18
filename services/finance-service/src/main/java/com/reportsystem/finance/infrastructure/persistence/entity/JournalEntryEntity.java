package com.reportsystem.finance.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;


@Entity
@Table(name = "journal_entries")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class JournalEntryEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "entry_number") private String entryNumber;
    @Column(name = "entry_date") private LocalDate entryDate;
    private String description;
    @Column(name = "reference_type") private String referenceType;
    @Column(name = "reference_id") private UUID referenceId;
    @Column(name = "is_posted") private boolean posted;
    @Column(name = "posted_at") private Instant postedAt;
    @Column(name = "created_by") private UUID createdBy;
    @Column(name = "branch_id", nullable = false) private UUID branchId;
    @Column(name = "from_branch_id") private UUID fromBranchId;
    @Column(name = "to_branch_id") private UUID toBranchId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
}
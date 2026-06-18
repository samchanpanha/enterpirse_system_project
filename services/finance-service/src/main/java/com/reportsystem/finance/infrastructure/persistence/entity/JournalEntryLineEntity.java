package com.reportsystem.finance.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;


@Entity
@Table(name = "journal_entry_lines")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class JournalEntryLineEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "journal_entry_id", nullable = false) private UUID journalEntryId;
    @Column(name = "account_id", nullable = false) private UUID accountId;
    private BigDecimal debit; private BigDecimal credit;
    private String description;
    @Column(name = "branch_id", nullable = false) private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
}
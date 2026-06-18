package com.reportsystem.finance.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class JournalEntry {
    private UUID id;
    private UUID tenantId;
    private UUID branchId;
    private String entryNumber;
    private java.time.LocalDate entryDate;
    private String description;
    private String referenceType;
    private UUID referenceId;
    private UUID fromBranchId;
    private UUID toBranchId;
    private boolean posted;
    private Instant postedAt;
    private UUID createdBy;
    private Instant createdAt;
    private Instant updatedAt;
}

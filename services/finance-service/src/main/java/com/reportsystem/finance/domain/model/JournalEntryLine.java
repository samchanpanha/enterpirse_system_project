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
public class JournalEntryLine {
    private UUID id;
    private UUID journalEntryId;
    private UUID accountId;
    private UUID branchId;
    private BigDecimal debit;
    private BigDecimal credit;
    private String description;
    private Instant createdAt;
}

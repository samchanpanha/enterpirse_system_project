package com.reportsystem.payment.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class ReconciliationRecord {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private String gateway;
    private java.time.LocalDate statementDate;
    private BigDecimal totalExpected;
    private BigDecimal totalMatched;
    private BigDecimal totalUnmatched;
    private String status;
    private int matchedCount;
    private int unmatchedCount;
    private Instant processedAt;
    private final Instant createdAt;
}

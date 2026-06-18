package com.reportsystem.finance.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class TaxRecord {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private String taxType;
    private int periodMonth;
    private int periodYear;
    private BigDecimal taxableAmount;
    private BigDecimal taxRate;
    private BigDecimal taxAmount;
    private String sourceType;
    private UUID sourceId;
    private final Instant createdAt;
}
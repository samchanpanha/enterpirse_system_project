package com.reportsystem.finance.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class PayrollPeriod {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private int periodMonth;
    private int periodYear;
    private String periodType;
    private java.time.LocalDate startDate;
    private java.time.LocalDate endDate;
    private java.time.LocalDate paymentDate;
    private String status;
    private final Instant createdAt;
}
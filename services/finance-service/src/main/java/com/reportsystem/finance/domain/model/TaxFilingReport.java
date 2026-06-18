package com.reportsystem.finance.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class TaxFilingReport {
    private final UUID id;
    private final UUID tenantId;
    private String taxType;
    private Integer periodMonth;
    private int periodYear;
    private String periodType;
    private BigDecimal totalTax;
    private String status;
    private java.time.LocalDate filedDate;
    private String referenceNumber;
    private String exportFormat;
    private String exportUrl;
    private final Instant createdAt;
    private Instant updatedAt;
}
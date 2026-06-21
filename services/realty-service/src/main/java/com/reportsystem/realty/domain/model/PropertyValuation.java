package com.reportsystem.realty.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PropertyValuation {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private UUID propertyId;
    private BigDecimal estimatedValue;
    private BigDecimal lowRange;
    private BigDecimal highRange;
    private BigDecimal confidenceScore;
    private LocalDate valuationDate;
    private String factors;
    private final Instant createdAt;
}

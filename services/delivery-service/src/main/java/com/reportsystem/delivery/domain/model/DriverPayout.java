package com.reportsystem.delivery.domain.model;

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
public class DriverPayout {
    private final UUID id;
    private final UUID tenantId;
    private final UUID driverId;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Integer totalDeliveries;
    private BigDecimal totalDistance;
    private BigDecimal totalEarnings;
    private BigDecimal commissionAmount;
    private BigDecimal bonusAmount;
    private BigDecimal deductionAmount;
    private BigDecimal netAmount;
    private String status;
    private Instant paidAt;
    private final Instant createdAt;
    private Instant updatedAt;
}

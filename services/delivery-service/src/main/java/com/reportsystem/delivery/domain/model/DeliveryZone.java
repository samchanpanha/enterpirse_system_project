package com.reportsystem.delivery.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class DeliveryZone {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private String name;
    private String nameKh;
    private String boundaries;
    private BigDecimal baseFee;
    private BigDecimal perKmFee;
    private BigDecimal minFee;
    private BigDecimal maxFee;
    private Integer estimatedMinutes;
    private boolean isActive;
    private final Instant createdAt;
    private Instant updatedAt;
}

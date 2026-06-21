package com.reportsystem.realty.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Commission {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private final UUID offerId;
    private UUID agentId;
    private BigDecimal amount;
    private BigDecimal percentage;
    private String status;
    private Instant paidAt;
    private final Instant createdAt;
}

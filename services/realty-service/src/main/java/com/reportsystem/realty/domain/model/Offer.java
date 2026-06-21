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
public class Offer {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private final UUID listingId;
    private UUID buyerId;
    private UUID agentId;
    private BigDecimal amount;
    private String currency;
    private String terms;
    private String status;
    private LocalDate expiryDate;
    private BigDecimal counterAmount;
    private String rejectedReason;
    private final Instant createdAt;
    private Instant updatedAt;
}

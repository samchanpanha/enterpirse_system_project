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
public class Lead {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private UUID listingId;
    private UUID agentId;
    private String name;
    private String phone;
    private String email;
    private String message;
    private String source;
    private String status;
    private BigDecimal budgetMin;
    private BigDecimal budgetMax;
    private String propertyTypePreference;
    private String preferredDistrict;
    private String notes;
    private final Instant createdAt;
    private Instant updatedAt;
}

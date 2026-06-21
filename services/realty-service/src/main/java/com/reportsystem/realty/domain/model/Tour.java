package com.reportsystem.realty.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Tour {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private UUID listingId;
    private UUID agentId;
    private UUID leadId;
    private Instant scheduledAt;
    private String status;
    private String notes;
    private final Instant createdAt;
    private Instant updatedAt;
}

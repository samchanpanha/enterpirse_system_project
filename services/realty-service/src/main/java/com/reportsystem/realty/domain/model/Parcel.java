package com.reportsystem.realty.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Parcel {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private UUID residentId;
    private String carrier;
    private String trackingNumber;
    private String description;
    private String status;
    private Instant receivedAt;
    private Instant pickedUpAt;
    private String notes;
    private final Instant createdAt;
}

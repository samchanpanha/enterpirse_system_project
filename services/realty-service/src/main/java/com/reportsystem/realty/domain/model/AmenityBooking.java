package com.reportsystem.realty.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class AmenityBooking {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private UUID residentId;
    private String amenityType;
    private LocalDate bookedDate;
    private LocalTime startTime;
    private LocalTime endTime;
    private int guests;
    private String status;
    private String notes;
    private final Instant createdAt;
    private Instant updatedAt;
}

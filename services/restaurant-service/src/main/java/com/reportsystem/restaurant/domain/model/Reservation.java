package com.reportsystem.restaurant.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Reservation {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private final UUID outletId;
    private final UUID tableId;
    private final UUID customerId;
    private String guestName;
    private String guestPhone;
    private int guestCount;
    private Instant reservationTime;
    private int durationMinutes;
    private String status;
    private String notes;
    private final Instant createdAt;
    private Instant updatedAt;
}

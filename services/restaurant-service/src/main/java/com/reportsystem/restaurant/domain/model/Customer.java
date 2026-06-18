package com.reportsystem.restaurant.domain.model;

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
public class Customer {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private final UUID outletId;
    private String name;
    private String phone;
    private String email;
    private LocalDate birthday;
    private boolean vip;
    private String notes;
    private int totalVisits;
    private BigDecimal totalSpent;
    private Instant lastVisitAt;
    private final Instant createdAt;
    private Instant updatedAt;
}

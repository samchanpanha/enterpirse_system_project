package com.reportsystem.property.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Property {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private String name;
    private String type;
    private String address;
    private String city;
    private String district;
    private BigDecimal lat;
    private BigDecimal lng;
    private int totalUnits;
    private String status;
    private String ownerName;
    private String ownerPhone;
    private String notes;
    private final Instant createdAt;
    private Instant updatedAt;
}

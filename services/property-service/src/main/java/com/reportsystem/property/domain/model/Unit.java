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
public class Unit {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private final UUID propertyId;
    private String label;
    private Integer floor;
    private Integer bedrooms;
    private Integer bathrooms;
    private BigDecimal areaSqm;
    private BigDecimal rentAmount;
    private BigDecimal depositAmount;
    private String currency;
    private String status;
    private String type;
    private String amenities;
    private String images;
    private final Instant createdAt;
    private Instant updatedAt;

    public void markVacant() { this.status = "vacant"; }
    public void markOccupied() { this.status = "occupied"; }
    public void markMaintenance() { this.status = "maintenance"; }
}

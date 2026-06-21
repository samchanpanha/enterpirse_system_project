package com.reportsystem.delivery.domain.model;

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
public class FleetVehicle {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private String name;
    private String plateNumber;
    private String vehicleType;
    private String status;
    private BigDecimal capacityKg;
    private String fuelType;
    private LocalDate insuranceExpiry;
    private LocalDate lastMaintenanceAt;
    private LocalDate nextMaintenanceAt;
    private boolean isActive;
    private final Instant createdAt;
    private Instant updatedAt;
}

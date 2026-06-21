package com.reportsystem.delivery.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Driver {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private String name;
    private String phone;
    private String email;
    private String licenseNumber;
    private String vehicleType;
    private String vehiclePlate;
    private String status;
    private BigDecimal rating;
    private Integer totalDeliveries;
    private boolean isActive;
    private BigDecimal currentLat;
    private BigDecimal currentLng;
    private Instant lastLocationAt;
    private final Instant createdAt;
    private Instant updatedAt;
}

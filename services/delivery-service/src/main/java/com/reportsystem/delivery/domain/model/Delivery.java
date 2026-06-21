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
public class Delivery {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private final UUID outletId;
    private final UUID orderId;
    private final UUID driverId;
    private String customerName;
    private String customerPhone;
    private String deliveryAddress;
    private String pickupAddress;
    private String status;
    private Instant scheduledAt;
    private Instant pickedUpAt;
    private Instant deliveredAt;
    private BigDecimal deliveryFee;
    private BigDecimal distanceKm;
    private String notes;
    private String metadata;
    private final Instant createdAt;
    private Instant updatedAt;
}

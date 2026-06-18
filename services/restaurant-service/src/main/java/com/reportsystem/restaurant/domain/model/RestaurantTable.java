package com.reportsystem.restaurant.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class RestaurantTable {
    private final UUID id;
    private final UUID tenantId;
    private final UUID outletId;
    private String label;
    private int capacity;
    private String floor;
    private String section;
    private Double posX;
    private Double posY;
    private String status;
    private String qrCodeUrl;
    private final Instant createdAt;
    private Instant updatedAt;
}

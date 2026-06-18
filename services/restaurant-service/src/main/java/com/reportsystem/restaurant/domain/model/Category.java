package com.reportsystem.restaurant.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Category {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private final UUID outletId;
    private String name;
    private String description;
    private int sortOrder;
    private boolean active;
    private final Instant createdAt;
}

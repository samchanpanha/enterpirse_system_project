package com.reportsystem.restaurant.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MenuItem {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private final UUID categoryId;
    private String name;
    private String nameKh;
    private String description;
    private String descriptionKh;
    private BigDecimal price;
    private String currency;
    private BigDecimal taxRate;
    private String imageUrl;
    private String options;
    private String modifiers;
    private boolean active;
    private int sortOrder;
    private final Instant createdAt;
    private Instant updatedAt;
}

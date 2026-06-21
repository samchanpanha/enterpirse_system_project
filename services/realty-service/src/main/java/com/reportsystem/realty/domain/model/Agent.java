package com.reportsystem.realty.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Agent {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private UUID userId;
    private String name;
    private String nameKh;
    private String phone;
    private String email;
    private String licenseNumber;
    private String bio;
    private String avatarUrl;
    private BigDecimal rating;
    private int totalSales;
    private int totalListings;
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;
}

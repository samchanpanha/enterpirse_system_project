package com.reportsystem.restaurant.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Outlet {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private String name;
    private String address;
    private String phone;
    private String email;
    private String taxNumber;
    private String type;
    private String currency;
    private String settings;
    private boolean active;
    private final Instant createdAt;
    private Instant updatedAt;
}

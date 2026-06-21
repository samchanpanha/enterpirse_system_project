package com.reportsystem.realty.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Customer {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private String name;
    private String phone;
    private String email;
    private String type;
    private String notes;
    private final Instant createdAt;
    private Instant updatedAt;
}

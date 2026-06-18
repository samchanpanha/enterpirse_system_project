package com.reportsystem.auth.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Role {
    private final UUID id;
    private final UUID tenantId;
    private final String name;
    private final String description;
    private final boolean system;
    private final Instant createdAt;
}

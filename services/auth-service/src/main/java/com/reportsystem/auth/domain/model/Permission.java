package com.reportsystem.auth.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Permission {
    private final UUID id;
    private final String code;
    private final String name;
    private final String module;
    private final Instant createdAt;
}

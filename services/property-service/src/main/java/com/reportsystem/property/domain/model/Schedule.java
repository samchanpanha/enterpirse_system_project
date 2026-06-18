package com.reportsystem.property.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Schedule {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private final UUID unitId;
    private String title;
    private String description;
    private String type;
    private String intervalType;
    private Instant startTime;
    private Instant endTime;
    private String recurringRule;
    private String status;
    private UUID createdBy;
    private final Instant createdAt;
    private Instant updatedAt;
}

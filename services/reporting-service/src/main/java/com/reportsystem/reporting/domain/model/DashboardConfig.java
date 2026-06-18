package com.reportsystem.reporting.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class DashboardConfig {
    private final UUID id; private final UUID tenantId; private final UUID branchId;
    private String name; private String layout;
    private boolean isDefault; private UUID createdBy;
    private final Instant createdAt; private Instant updatedAt;
}
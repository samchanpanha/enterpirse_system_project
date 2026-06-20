package com.reportsystem.reporting.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class ReportDefinition {
    private final UUID id; private final UUID tenantId; private final UUID branchId;
    private String name; private String code; private String type;
    private String config; private String layout; private boolean system;
    private final Instant createdAt; private Instant updatedAt;
}
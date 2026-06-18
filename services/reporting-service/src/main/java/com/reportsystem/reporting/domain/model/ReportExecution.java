package com.reportsystem.reporting.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class ReportExecution {
    private final UUID id; private UUID reportId;
    private final UUID tenantId; private String parameters;
    private String status; private String outputUrl;
    private Integer rowCount; private Integer durationMs;
    private String errorMessage; private UUID requestedBy;
    private Instant startedAt; private Instant completedAt;
    private final Instant createdAt;
}
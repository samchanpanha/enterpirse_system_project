package com.reportsystem.reporting.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class ScheduledReport {
    private final UUID id; private final UUID tenantId;
    private UUID reportId; private String scheduleCron;
    private String recipients; private String format;
    private boolean active; private Instant lastSentAt;
    private final Instant createdAt;
}
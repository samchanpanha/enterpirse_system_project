package com.reportsystem.reporting.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class AggregatedSnapshot {
    private final UUID id; private final UUID tenantId;
    private String snapshotType; private LocalDate snapshotDate;
    private String data;
    private final Instant createdAt;
}

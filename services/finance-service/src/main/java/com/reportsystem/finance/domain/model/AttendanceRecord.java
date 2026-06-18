package com.reportsystem.finance.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class AttendanceRecord {
    private final UUID id;
    private final UUID tenantId;
    private UUID employeeId;
    private java.time.LocalDate date;
    private Instant clockIn;
    private Instant clockOut;
    private Instant breakStart;
    private Instant breakEnd;
    private BigDecimal totalHours;
    private BigDecimal overtimeHours;
    private String status;
    private String notes;
    private final Instant createdAt;
}
package com.reportsystem.finance.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;


@Entity
@Table(name = "attendance_records")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AttendanceRecordEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "employee_id", nullable = false) private UUID employeeId;
    private LocalDate date; @Column(name = "clock_in") private Instant clockIn;
    @Column(name = "clock_out") private Instant clockOut;
    @Column(name = "break_start") private Instant breakStart;
    @Column(name = "break_end") private Instant breakEnd;
    @Column(name = "total_hours") private BigDecimal totalHours;
    @Column(name = "overtime_hours") private BigDecimal overtimeHours;
    private String status; private String notes;
    @Column(name = "branch_id", nullable = false) private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
}
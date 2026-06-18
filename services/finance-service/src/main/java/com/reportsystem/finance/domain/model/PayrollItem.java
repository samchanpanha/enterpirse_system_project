package com.reportsystem.finance.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class PayrollItem {
    private final UUID id;
    private final UUID payrollPeriodId;
    private UUID employeeId;
    private BigDecimal baseSalary;
    private String allowances;
    private BigDecimal overtimeAmount;
    private BigDecimal grossSalary;
    private BigDecimal tosAmount;
    private BigDecimal tofbAmount;
    private BigDecimal nssfEmployee;
    private BigDecimal nssfEmployer;
    private String deductions;
    private BigDecimal netSalary;
    private final Instant createdAt;
}

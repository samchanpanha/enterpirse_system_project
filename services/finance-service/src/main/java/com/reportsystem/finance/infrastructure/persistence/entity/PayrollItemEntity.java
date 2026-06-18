package com.reportsystem.finance.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;


@Entity
@Table(name = "payroll_items")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PayrollItemEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "payroll_period_id", nullable = false) private UUID payrollPeriodId;
    @Column(name = "employee_id", nullable = false) private UUID employeeId;
    @Column(name = "base_salary") private BigDecimal baseSalary;
    private String allowances;
    @Column(name = "overtime_amount") private BigDecimal overtimeAmount;
    @Column(name = "gross_salary") private BigDecimal grossSalary;
    @Column(name = "tos_amount") private BigDecimal tosAmount;
    @Column(name = "tofb_amount") private BigDecimal tofbAmount;
    @Column(name = "nssf_employee") private BigDecimal nssfEmployee;
    @Column(name = "nssf_employer") private BigDecimal nssfEmployer;
    private String deductions;
    @Column(name = "net_salary") private BigDecimal netSalary;
    @Column(name = "branch_id", nullable = false) private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
}
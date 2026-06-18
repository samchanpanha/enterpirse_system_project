package com.reportsystem.finance.domain.service;

import com.reportsystem.finance.domain.model.*;
import com.reportsystem.finance.domain.port.inbound.*;
import com.reportsystem.finance.domain.port.outbound.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


public class PayrollServiceImpl implements PayrollService {
    private final EmployeeRepository empRepo;
    private final PayrollPeriodRepository ppRepo;
    private final PayrollItemRepository piRepo;
    public PayrollServiceImpl(EmployeeRepository er, PayrollPeriodRepository ppr, PayrollItemRepository pir) { empRepo = er; ppRepo = ppr; piRepo = pir; }

    @Override public Employee createEmployee(UUID tenantId, UUID branchId, String firstName, String lastName, BigDecimal baseSalary) {
        return empRepo.save(Employee.builder().id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId).firstName(firstName).lastName(lastName).baseSalary(baseSalary).status("active").hireDate(LocalDate.now()).createdAt(Instant.now()).build());
    }
    @Override public List<Employee> getEmployeesByTenant(UUID tenantId) { return empRepo.findByTenantId(tenantId); }
    @Override public List<Employee> getEmployeesByTenantAndBranch(UUID tenantId, UUID branchId) { return empRepo.findByTenantIdAndBranchId(tenantId, branchId); }
    @Override public PayrollPeriod createPayrollPeriod(UUID tenantId, UUID branchId, int month, int year) {
        LocalDate start = LocalDate.of(year, month, 1);
        LocalDate end = start.withDayOfMonth(start.lengthOfMonth());
        return ppRepo.save(PayrollPeriod.builder().id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId).periodMonth(month).periodYear(year).periodType("monthly").startDate(start).endDate(end).status("open").createdAt(Instant.now()).build());
    }
    @Override public List<PayrollItem> runPayroll(UUID payrollPeriodId) {
        PayrollPeriod pp = ppRepo.findById(payrollPeriodId).orElseThrow();
        List<Employee> employees = empRepo.findByTenantId(pp.getTenantId());
        List<PayrollItem> items = new ArrayList<>();
        for (Employee emp : employees) {
            BigDecimal base = emp.getBaseSalary();
            BigDecimal tos = computeTos(base);
            BigDecimal nssfEmp = base.multiply(BigDecimal.valueOf(0.02)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal nssfEmpr = base.multiply(BigDecimal.valueOf(0.026)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal net = base.subtract(tos).subtract(nssfEmp);
            items.add(piRepo.save(PayrollItem.builder().id(UUID.randomUUID()).payrollPeriodId(payrollPeriodId).employeeId(emp.getId())
                    .baseSalary(base).grossSalary(base).tosAmount(tos).nssfEmployee(nssfEmp).nssfEmployer(nssfEmpr)
                    .deductions("[]").allowances("[]").netSalary(net).createdAt(Instant.now()).build()));
        }
        return items;
    }
    private BigDecimal computeTos(BigDecimal salary) {
        if (salary.compareTo(BigDecimal.valueOf(1_300_000)) <= 0) return BigDecimal.ZERO;
        if (salary.compareTo(BigDecimal.valueOf(2_000_000)) <= 0) return salary.subtract(BigDecimal.valueOf(1_300_000)).multiply(BigDecimal.valueOf(0.05)).setScale(2, RoundingMode.HALF_UP);
        if (salary.compareTo(BigDecimal.valueOf(8_500_000)) <= 0) return BigDecimal.valueOf(35_000).add(salary.subtract(BigDecimal.valueOf(2_000_000)).multiply(BigDecimal.valueOf(0.10))).setScale(2, RoundingMode.HALF_UP);
        if (salary.compareTo(BigDecimal.valueOf(12_500_000)) <= 0) return BigDecimal.valueOf(685_000).add(salary.subtract(BigDecimal.valueOf(8_500_000)).multiply(BigDecimal.valueOf(0.15))).setScale(2, RoundingMode.HALF_UP);
        return BigDecimal.valueOf(1_285_000).add(salary.subtract(BigDecimal.valueOf(12_500_000)).multiply(BigDecimal.valueOf(0.20))).setScale(2, RoundingMode.HALF_UP);
    }
}
package com.reportsystem.finance.domain.port.inbound;

import com.reportsystem.finance.domain.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PayrollService {
    Employee createEmployee(UUID tenantId, UUID branchId, String firstName, String lastName, BigDecimal baseSalary);
    List<Employee> getEmployeesByTenant(UUID tenantId);
    List<Employee> getEmployeesByTenantAndBranch(UUID tenantId, UUID branchId);
    PayrollPeriod createPayrollPeriod(UUID tenantId, UUID branchId, int month, int year);
    List<PayrollItem> runPayroll(UUID payrollPeriodId);
}

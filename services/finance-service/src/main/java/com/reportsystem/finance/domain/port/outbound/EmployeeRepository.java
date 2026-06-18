package com.reportsystem.finance.domain.port.outbound;

import com.reportsystem.finance.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EmployeeRepository {
    Employee save(Employee e);
    Optional<Employee> findById(UUID id);
    List<Employee> findByTenantId(UUID tenantId);
    List<Employee> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

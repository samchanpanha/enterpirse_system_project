package com.reportsystem.reporting.domain.port.outbound;

import com.reportsystem.reporting.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DashboardConfigRepository {
    DashboardConfig save(DashboardConfig d);
    List<DashboardConfig> findByTenantId(UUID tenantId);
    List<DashboardConfig> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

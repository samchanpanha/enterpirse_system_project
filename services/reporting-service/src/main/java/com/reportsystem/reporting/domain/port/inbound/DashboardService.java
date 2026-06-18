package com.reportsystem.reporting.domain.port.inbound;

import com.reportsystem.reporting.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DashboardService {
    DashboardConfig createDashboard(UUID tenantId, UUID branchId, String name, String layout, UUID createdBy);
    List<DashboardConfig> getDashboardsByTenant(UUID tenantId);
    List<DashboardConfig> getDashboardsByTenantAndBranch(UUID tenantId, UUID branchId);
}

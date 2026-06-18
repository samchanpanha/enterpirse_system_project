package com.reportsystem.reporting.domain.service;

import com.reportsystem.reporting.domain.model.*;
import com.reportsystem.reporting.domain.port.inbound.*;
import com.reportsystem.reporting.domain.port.outbound.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class DashboardServiceImpl implements DashboardService {
    private final DashboardConfigRepository dashRepo;
    public DashboardServiceImpl(DashboardConfigRepository dashRepo) { this.dashRepo = dashRepo; }

    @Override public DashboardConfig createDashboard(UUID tenantId, UUID branchId, String name, String layout, UUID createdBy) {
        return dashRepo.save(DashboardConfig.builder().id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId).name(name).layout(layout != null ? layout : "{}").isDefault(false).createdBy(createdBy).createdAt(Instant.now()).build());
    }
    @Override public List<DashboardConfig> getDashboardsByTenant(UUID tenantId) { return dashRepo.findByTenantId(tenantId); }
    @Override public List<DashboardConfig> getDashboardsByTenantAndBranch(UUID tenantId, UUID branchId) { return dashRepo.findByTenantIdAndBranchId(tenantId, branchId); }
}
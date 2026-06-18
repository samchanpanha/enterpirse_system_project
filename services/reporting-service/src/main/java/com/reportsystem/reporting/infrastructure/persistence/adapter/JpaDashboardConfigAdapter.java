package com.reportsystem.reporting.infrastructure.persistence.adapter;

import com.reportsystem.reporting.domain.model.*;
import com.reportsystem.reporting.domain.port.outbound.*;
import com.reportsystem.reporting.infrastructure.persistence.entity.*;
import com.reportsystem.reporting.infrastructure.persistence.repository.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class JpaDashboardConfigAdapter implements DashboardConfigRepository {
    private final JpaDashboardConfigRepository repo;
    public JpaDashboardConfigAdapter(JpaDashboardConfigRepository r) { repo = r; }
    @Override public DashboardConfig save(DashboardConfig d) {
        DashboardConfigEntity e = new DashboardConfigEntity(); e.setId(d.getId()); e.setTenantId(d.getTenantId()); e.setBranchId(d.getBranchId());
        e.setName(d.getName()); e.setLayout(d.getLayout() != null ? d.getLayout() : "{}"); e.setDefault(d.isDefault());
        e.setCreatedBy(d.getCreatedBy()); e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e)); }
    @Override public List<DashboardConfig> findByTenantId(UUID t) { return repo.findByTenantId(t).stream().map(this::toDomain).toList(); }
    @Override public List<DashboardConfig> findByTenantIdAndBranchId(UUID t, UUID b) { return repo.findByTenantIdAndBranchId(t, b).stream().map(this::toDomain).toList(); }
    private DashboardConfig toDomain(DashboardConfigEntity e) { return DashboardConfig.builder().id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId()).name(e.getName()).layout(e.getLayout()).isDefault(e.isDefault()).createdBy(e.getCreatedBy()).createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).build(); }
}
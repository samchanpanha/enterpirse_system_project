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


public class JpaReportDefinitionAdapter implements ReportDefinitionRepository {
    private final JpaReportDefinitionRepository repo;
    public JpaReportDefinitionAdapter(JpaReportDefinitionRepository r) { repo = r; }
    @Override public ReportDefinition save(ReportDefinition d) {
        ReportDefinitionEntity e = new ReportDefinitionEntity(); e.setId(d.getId()); e.setTenantId(d.getTenantId()); e.setBranchId(d.getBranchId());
        e.setName(d.getName()); e.setCode(d.getCode()); e.setType(d.getType()); e.setConfig(d.getConfig() != null ? d.getConfig() : "{}");
        e.setLayout(d.getLayout() != null ? d.getLayout() : "[]"); e.setSystem(d.isSystem()); e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e)); }
    @Override public Optional<ReportDefinition> findById(UUID id) { return repo.findById(id).map(this::toDomain); }
    @Override public List<ReportDefinition> findByTenantId(UUID t) { return repo.findByTenantId(t).stream().map(this::toDomain).toList(); }
    @Override public List<ReportDefinition> findByTenantIdAndBranchId(UUID t, UUID b) { return repo.findByTenantIdAndBranchId(t, b).stream().map(this::toDomain).toList(); }
    private ReportDefinition toDomain(ReportDefinitionEntity e) { return ReportDefinition.builder().id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId()).name(e.getName()).code(e.getCode()).type(e.getType()).config(e.getConfig()).layout(e.getLayout()).system(e.isSystem()).createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).build(); }
}
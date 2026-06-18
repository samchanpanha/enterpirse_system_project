package com.reportsystem.reporting.domain.service;

import com.reportsystem.reporting.domain.model.*;
import com.reportsystem.reporting.domain.port.inbound.*;
import com.reportsystem.reporting.domain.port.outbound.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class ReportServiceImpl implements ReportService {
    private final ReportDefinitionRepository defRepo;
    private final ReportExecutionRepository execRepo;
    public ReportServiceImpl(ReportDefinitionRepository defRepo, ReportExecutionRepository execRepo) {
        this.defRepo = defRepo; this.execRepo = execRepo;
    }

    @Override public ReportDefinition createDefinition(UUID tenantId, UUID branchId, String name, String type, String config) {
        return defRepo.save(ReportDefinition.builder().id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId).name(name).type(type).config(config != null ? config : "{}").system(false).createdAt(Instant.now()).build());
    }
    @Override public Optional<ReportDefinition> getDefinitionById(UUID id) { return defRepo.findById(id); }
    @Override public List<ReportDefinition> getDefinitionsByTenant(UUID tenantId) { return defRepo.findByTenantId(tenantId); }
    @Override public List<ReportDefinition> getDefinitionsByTenantAndBranch(UUID tenantId, UUID branchId) { return defRepo.findByTenantIdAndBranchId(tenantId, branchId); }
    @Override public ReportExecution executeReport(UUID reportId, UUID tenantId, String parameters, UUID requestedBy) {
        ReportDefinition def = defRepo.findById(reportId).orElseThrow();
        ReportExecution exec = ReportExecution.builder().id(UUID.randomUUID()).reportId(reportId).tenantId(tenantId)
                .parameters(parameters != null ? parameters : "{}").status("running").startedAt(Instant.now()).requestedBy(requestedBy).createdAt(Instant.now()).build();
        exec = execRepo.save(exec);
        // Stub: run the actual report based on def.getType() and def.getConfig()
        exec = ReportExecution.builder().id(exec.getId()).reportId(reportId).tenantId(tenantId).parameters(exec.getParameters())
                .status("completed").outputUrl("/reports/output/" + exec.getId() + ".pdf").rowCount(0).durationMs(100)
                .requestedBy(requestedBy).startedAt(exec.getStartedAt()).completedAt(Instant.now()).createdAt(exec.getCreatedAt()).build();
        return execRepo.save(exec);
    }
}
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


public class JpaReportExecutionAdapter implements ReportExecutionRepository {
    private final JpaReportExecutionRepository repo;
    public JpaReportExecutionAdapter(JpaReportExecutionRepository r) { repo = r; }
    @Override public ReportExecution save(ReportExecution e) {
        ReportExecutionEntity en = new ReportExecutionEntity(); en.setId(e.getId()); en.setReportId(e.getReportId());
        en.setTenantId(e.getTenantId()); en.setParameters(e.getParameters()); en.setStatus(e.getStatus());
        en.setOutputUrl(e.getOutputUrl()); en.setRowCount(e.getRowCount()); en.setDurationMs(e.getDurationMs());
        en.setErrorMessage(e.getErrorMessage()); en.setRequestedBy(e.getRequestedBy()); en.setStartedAt(e.getStartedAt());
        en.setCompletedAt(e.getCompletedAt()); en.setCreatedAt(Instant.now());
        return toDomain(repo.save(en)); }
    @Override public Optional<ReportExecution> findById(UUID id) { return repo.findById(id).map(this::toDomain); }
    @Override public List<ReportExecution> findByTenantId(UUID t) { return repo.findByTenantId(t).stream().map(this::toDomain).toList(); }
    private ReportExecution toDomain(ReportExecutionEntity e) { return ReportExecution.builder().id(e.getId()).reportId(e.getReportId()).tenantId(e.getTenantId()).parameters(e.getParameters()).status(e.getStatus()).outputUrl(e.getOutputUrl()).rowCount(e.getRowCount()).durationMs(e.getDurationMs()).errorMessage(e.getErrorMessage()).requestedBy(e.getRequestedBy()).startedAt(e.getStartedAt()).completedAt(e.getCompletedAt()).createdAt(e.getCreatedAt()).build(); }
}
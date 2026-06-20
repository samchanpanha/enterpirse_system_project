package com.reportsystem.reporting.domain.port.inbound;

import com.reportsystem.reporting.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportService {
    ReportDefinition createDefinition(UUID tenantId, UUID branchId, String name, String type, String config, String layout);
    Optional<ReportDefinition> getDefinitionById(UUID id);
    List<ReportDefinition> getDefinitionsByTenant(UUID tenantId);
    List<ReportDefinition> getDefinitionsByTenantAndBranch(UUID tenantId, UUID branchId);
    ReportExecution executeReport(UUID reportId, UUID tenantId, UUID branchId, String parameters, UUID requestedBy);
    Optional<ReportExecution> getExecutionById(UUID id);
    List<ReportExecution> getExecutionsByReportId(UUID reportId);
    List<ReportExecution> getExecutionsByReportIdAndBranch(UUID reportId, UUID branchId);
}

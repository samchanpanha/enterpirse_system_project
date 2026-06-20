package com.reportsystem.reporting.domain.port.outbound;

import com.reportsystem.reporting.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportExecutionRepository {
    ReportExecution save(ReportExecution e);
    Optional<ReportExecution> findById(UUID id);
    List<ReportExecution> findByTenantId(UUID tenantId);
    List<ReportExecution> findByReportId(UUID reportId);
    List<ReportExecution> findByReportIdAndBranchId(UUID reportId, UUID branchId);
}

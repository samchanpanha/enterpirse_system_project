package com.reportsystem.reporting.domain.port.outbound;

import com.reportsystem.reporting.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReportDefinitionRepository {
    ReportDefinition save(ReportDefinition d);
    Optional<ReportDefinition> findById(UUID id);
    List<ReportDefinition> findByTenantId(UUID tenantId);
    List<ReportDefinition> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

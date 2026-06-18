package com.reportsystem.reporting.domain.port.outbound;

import com.reportsystem.reporting.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScheduledReportRepository {
    ScheduledReport save(ScheduledReport s);
    List<ScheduledReport> findByTenantId(UUID tenantId);
}

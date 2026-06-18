package com.reportsystem.reporting.domain.port.outbound;

import com.reportsystem.reporting.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AggregatedSnapshotRepository {
    AggregatedSnapshot save(AggregatedSnapshot s);
    Optional<AggregatedSnapshot> findByTenantIdAndSnapshotTypeAndSnapshotDate(UUID tenantId, String type, java.time.LocalDate date);
}

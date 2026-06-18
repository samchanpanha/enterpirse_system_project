package com.reportsystem.property.domain.port.outbound;

import com.reportsystem.property.domain.model.Schedule;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScheduleRepository {
    Schedule save(Schedule schedule);
    Optional<Schedule> findById(UUID id);
    List<Schedule> findByUnitIdAndTimeRange(UUID unitId, Instant from, Instant to);
    List<Schedule> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    List<Schedule> findByUnitIds(java.util.Collection<UUID> unitIds);
    void deleteById(UUID id);
    boolean hasOverlap(UUID unitId, Instant startTime, Instant endTime);
}

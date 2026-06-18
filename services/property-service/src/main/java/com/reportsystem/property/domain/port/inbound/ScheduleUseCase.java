package com.reportsystem.property.domain.port.inbound;

import com.reportsystem.property.domain.model.Schedule;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ScheduleUseCase {
    Schedule createSchedule(UUID tenantId, UUID branchId, UUID unitId, String title, String type,
                            String intervalType, Instant startTime, Instant endTime);
    Optional<Schedule> getScheduleById(UUID id);
    List<Schedule> getSchedulesByUnit(UUID unitId, Instant from, Instant to);
    List<Schedule> getSchedulesByProperty(UUID propertyId);
    Schedule updateSchedule(UUID id, String title, String description, Instant startTime, Instant endTime);
    void deleteSchedule(UUID id);
    boolean isUnitAvailable(UUID unitId, Instant startTime, Instant endTime);
}

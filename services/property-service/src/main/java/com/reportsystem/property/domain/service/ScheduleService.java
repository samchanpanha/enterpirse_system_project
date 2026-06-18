package com.reportsystem.property.domain.service;

import com.reportsystem.property.domain.model.Schedule;
import com.reportsystem.property.domain.port.inbound.ScheduleUseCase;
import com.reportsystem.property.domain.port.outbound.ScheduleRepository;
import com.reportsystem.property.domain.port.outbound.UnitRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class ScheduleService implements ScheduleUseCase {

    private final ScheduleRepository scheduleRepository;
    private final UnitRepository unitRepository;

    public ScheduleService(ScheduleRepository scheduleRepository, UnitRepository unitRepository) {
        this.scheduleRepository = scheduleRepository;
        this.unitRepository = unitRepository;
    }

    @Override
    public Schedule createSchedule(UUID tenantId, UUID branchId, UUID unitId, String title, String type,
                                    String intervalType, Instant startTime, Instant endTime) {
        if (scheduleRepository.hasOverlap(unitId, startTime, endTime)) {
            throw new IllegalArgumentException("Schedule conflict for unit: " + unitId);
        }
        Schedule schedule = Schedule.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId).unitId(unitId)
                .title(title).type(type).intervalType(intervalType)
                .startTime(startTime).endTime(endTime).status("scheduled")
                .createdAt(Instant.now())
                .build();
        return scheduleRepository.save(schedule);
    }

    @Override
    public Optional<Schedule> getScheduleById(UUID id) {
        return scheduleRepository.findById(id);
    }

    @Override
    public List<Schedule> getSchedulesByUnit(UUID unitId, Instant from, Instant to) {
        return scheduleRepository.findByUnitIdAndTimeRange(unitId, from, to);
    }

    @Override
    public List<Schedule> getSchedulesByProperty(UUID propertyId) {
        List<UUID> unitIds = unitRepository.findByPropertyId(propertyId).stream()
            .map(u -> u.getId())
            .collect(Collectors.toList());
        if (unitIds.isEmpty()) return List.of();
        return scheduleRepository.findByUnitIds(unitIds);
    }

    @Override
    public Schedule updateSchedule(UUID id, String title, String description, Instant startTime, Instant endTime) {
        Schedule existing = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Schedule not found: " + id));
        Schedule updated = Schedule.builder()
                .id(existing.getId()).tenantId(existing.getTenantId()).unitId(existing.getUnitId())
                .title(title != null ? title : existing.getTitle())
                .description(description != null ? description : existing.getDescription())
                .type(existing.getType()).intervalType(existing.getIntervalType())
                .startTime(startTime != null ? startTime : existing.getStartTime())
                .endTime(endTime != null ? endTime : existing.getEndTime())
                .recurringRule(existing.getRecurringRule()).status(existing.getStatus())
                .createdBy(existing.getCreatedBy())
                .createdAt(existing.getCreatedAt()).updatedAt(Instant.now())
                .build();
        return scheduleRepository.save(updated);
    }

    @Override
    public void deleteSchedule(UUID id) {
        scheduleRepository.deleteById(id);
    }

    @Override
    public boolean isUnitAvailable(UUID unitId, Instant startTime, Instant endTime) {
        return !scheduleRepository.hasOverlap(unitId, startTime, endTime);
    }
}

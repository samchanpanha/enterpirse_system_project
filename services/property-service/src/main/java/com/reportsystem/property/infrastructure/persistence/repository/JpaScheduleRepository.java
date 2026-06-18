package com.reportsystem.property.infrastructure.persistence.repository;

import com.reportsystem.property.infrastructure.persistence.entity.ScheduleEntity;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaScheduleRepository extends JpaRepository<ScheduleEntity, UUID> {
    @Query("SELECT s FROM ScheduleEntity s WHERE s.unitId = ?1 AND s.startTime >= ?2 AND s.endTime <= ?3")
    List<ScheduleEntity> findByUnitIdAndTimeRange(UUID unitId, Instant from, Instant to);
    List<ScheduleEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    List<ScheduleEntity> findByUnitIdIn(java.util.Collection<UUID> unitIds);
    @Query("SELECT COUNT(s) > 0 FROM ScheduleEntity s WHERE s.unitId = ?1 AND s.startTime < ?3 AND s.endTime > ?2 AND s.status <> 'cancelled'")
    boolean hasOverlap(UUID unitId, Instant startTime, Instant endTime);
}

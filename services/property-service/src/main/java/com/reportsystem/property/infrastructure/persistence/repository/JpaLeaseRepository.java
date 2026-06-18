package com.reportsystem.property.infrastructure.persistence.repository;

import com.reportsystem.property.infrastructure.persistence.entity.LeaseEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaLeaseRepository extends JpaRepository<LeaseEntity, UUID> {
    List<LeaseEntity> findByUnitId(UUID unitId);
    List<LeaseEntity> findByTenantId(UUID tenantId);
    List<LeaseEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    @Query("SELECT l FROM LeaseEntity l WHERE l.tenantId = ?1 AND l.status = 'active'")
    List<LeaseEntity> findActiveByTenantId(UUID tenantId);
    @Query("SELECT COUNT(l) > 0 FROM LeaseEntity l WHERE l.unitId = ?1 AND l.status = 'active' AND l.startDate <= ?3 AND l.endDate >= ?2")
    boolean hasOverlappingLease(UUID unitId, java.time.LocalDate startDate, java.time.LocalDate endDate);
}

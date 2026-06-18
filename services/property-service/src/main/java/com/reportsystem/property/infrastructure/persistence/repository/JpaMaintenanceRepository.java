package com.reportsystem.property.infrastructure.persistence.repository;

import com.reportsystem.property.infrastructure.persistence.entity.MaintenanceTicketEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMaintenanceRepository extends JpaRepository<MaintenanceTicketEntity, UUID> {
    List<MaintenanceTicketEntity> findByUnitId(UUID unitId);
    List<MaintenanceTicketEntity> findByTenantId(UUID tenantId);
    List<MaintenanceTicketEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    List<MaintenanceTicketEntity> findByUnitIdIn(java.util.Collection<UUID> unitIds);
}

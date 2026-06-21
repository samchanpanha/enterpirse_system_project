package com.reportsystem.delivery.infrastructure.persistence.repository;

import com.reportsystem.delivery.infrastructure.persistence.entity.*;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaFleetVehicleRepository extends JpaRepository<FleetVehicleEntity, UUID> {
    List<FleetVehicleEntity> findByTenantId(UUID tenantId);
    List<FleetVehicleEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

package com.reportsystem.delivery.domain.port.outbound;

import com.reportsystem.delivery.domain.model.FleetVehicle;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FleetVehicleRepository {
    FleetVehicle save(FleetVehicle vehicle);
    Optional<FleetVehicle> findById(UUID id);
    List<FleetVehicle> findByTenantId(UUID tenantId);
    List<FleetVehicle> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

package com.reportsystem.delivery.domain.port.inbound;

import com.reportsystem.delivery.domain.model.FleetVehicle;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface FleetService {
    FleetVehicle createVehicle(UUID tenantId, UUID branchId, String name, String plateNumber, String vehicleType);
    Optional<FleetVehicle> getVehicleById(UUID id);
    List<FleetVehicle> getVehiclesByTenant(UUID tenantId);
    List<FleetVehicle> getVehiclesByTenantAndBranch(UUID tenantId, UUID branchId);
    FleetVehicle updateStatus(UUID id, String status);
}

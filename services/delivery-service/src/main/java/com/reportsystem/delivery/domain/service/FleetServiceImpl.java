package com.reportsystem.delivery.domain.service;

import com.reportsystem.delivery.domain.model.FleetVehicle;
import com.reportsystem.delivery.domain.port.inbound.FleetService;
import com.reportsystem.delivery.domain.port.outbound.FleetVehicleRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class FleetServiceImpl implements FleetService {

    private final FleetVehicleRepository vehicleRepository;

    public FleetServiceImpl(FleetVehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    public FleetVehicle createVehicle(UUID tenantId, UUID branchId, String name, String plateNumber, String vehicleType) {
        FleetVehicle vehicle = FleetVehicle.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId)
                .name(name).plateNumber(plateNumber).vehicleType(vehicleType)
                .status("available").isActive(true).createdAt(Instant.now()).build();
        return vehicleRepository.save(vehicle);
    }

    @Override
    public Optional<FleetVehicle> getVehicleById(UUID id) {
        return vehicleRepository.findById(id);
    }

    @Override
    public List<FleetVehicle> getVehiclesByTenant(UUID tenantId) {
        return vehicleRepository.findByTenantId(tenantId);
    }

    @Override
    public List<FleetVehicle> getVehiclesByTenantAndBranch(UUID tenantId, UUID branchId) {
        return vehicleRepository.findByTenantIdAndBranchId(tenantId, branchId);
    }

    @Override
    public FleetVehicle updateStatus(UUID id, String status) {
        FleetVehicle existing = vehicleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("FleetVehicle not found: " + id));
        FleetVehicle updated = FleetVehicle.builder()
                .id(existing.getId()).tenantId(existing.getTenantId()).branchId(existing.getBranchId())
                .name(existing.getName()).plateNumber(existing.getPlateNumber())
                .vehicleType(existing.getVehicleType()).status(status)
                .capacityKg(existing.getCapacityKg()).fuelType(existing.getFuelType())
                .insuranceExpiry(existing.getInsuranceExpiry())
                .lastMaintenanceAt(existing.getLastMaintenanceAt())
                .nextMaintenanceAt(existing.getNextMaintenanceAt())
                .isActive(existing.isActive())
                .createdAt(existing.getCreatedAt()).updatedAt(Instant.now())
                .build();
        return vehicleRepository.save(updated);
    }
}

package com.reportsystem.delivery.infrastructure.persistence.adapter;

import com.reportsystem.delivery.domain.model.FleetVehicle;
import com.reportsystem.delivery.domain.port.outbound.FleetVehicleRepository;
import com.reportsystem.delivery.infrastructure.persistence.entity.FleetVehicleEntity;
import com.reportsystem.delivery.infrastructure.persistence.repository.JpaFleetVehicleRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaFleetVehicleAdapter implements FleetVehicleRepository {

    private final JpaFleetVehicleRepository repo;
    public JpaFleetVehicleAdapter(JpaFleetVehicleRepository repo) { this.repo = repo; }

    @Override
    public FleetVehicle save(FleetVehicle vehicle) {
        FleetVehicleEntity e = toEntity(vehicle);
        FleetVehicleEntity saved = repo.save(e);
        return toDomain(saved);
    }

    @Override
    public Optional<FleetVehicle> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<FleetVehicle> findByTenantId(UUID tenantId) {
        return repo.findByTenantId(tenantId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<FleetVehicle> findByTenantIdAndBranchId(UUID tenantId, UUID branchId) {
        return repo.findByTenantIdAndBranchId(tenantId, branchId).stream().map(this::toDomain).toList();
    }

    private FleetVehicleEntity toEntity(FleetVehicle v) {
        FleetVehicleEntity e = new FleetVehicleEntity();
        e.setId(v.getId());
        e.setTenantId(v.getTenantId());
        e.setBranchId(v.getBranchId());
        e.setName(v.getName());
        e.setPlateNumber(v.getPlateNumber());
        e.setVehicleType(v.getVehicleType());
        e.setStatus(v.getStatus());
        e.setCapacityKg(v.getCapacityKg());
        e.setFuelType(v.getFuelType());
        e.setInsuranceExpiry(v.getInsuranceExpiry());
        e.setLastMaintenanceAt(v.getLastMaintenanceAt());
        e.setNextMaintenanceAt(v.getNextMaintenanceAt());
        e.setActive(v.isActive());
        e.setCreatedAt(v.getCreatedAt());
        e.setUpdatedAt(Instant.now());
        return e;
    }

    private FleetVehicle toDomain(FleetVehicleEntity e) {
        return FleetVehicle.builder()
                .id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId())
                .name(e.getName()).plateNumber(e.getPlateNumber())
                .vehicleType(e.getVehicleType()).status(e.getStatus())
                .capacityKg(e.getCapacityKg()).fuelType(e.getFuelType())
                .insuranceExpiry(e.getInsuranceExpiry())
                .lastMaintenanceAt(e.getLastMaintenanceAt())
                .nextMaintenanceAt(e.getNextMaintenanceAt())
                .isActive(e.isActive())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }
}

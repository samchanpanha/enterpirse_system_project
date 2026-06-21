package com.reportsystem.delivery.infrastructure.persistence.adapter;

import com.reportsystem.delivery.domain.model.Driver;
import com.reportsystem.delivery.domain.port.outbound.DriverRepository;
import com.reportsystem.delivery.infrastructure.persistence.entity.DriverEntity;
import com.reportsystem.delivery.infrastructure.persistence.repository.JpaDriverRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaDriverAdapter implements DriverRepository {

    private final JpaDriverRepository repo;
    public JpaDriverAdapter(JpaDriverRepository repo) { this.repo = repo; }

    @Override
    public Driver save(Driver driver) {
        DriverEntity e = toEntity(driver);
        DriverEntity saved = repo.save(e);
        return toDomain(saved);
    }

    @Override
    public Optional<Driver> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Driver> findByTenantId(UUID tenantId) {
        return repo.findByTenantId(tenantId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Driver> findByTenantIdAndBranchId(UUID tenantId, UUID branchId) {
        return repo.findByTenantIdAndBranchId(tenantId, branchId).stream().map(this::toDomain).toList();
    }

    private DriverEntity toEntity(Driver d) {
        DriverEntity e = new DriverEntity();
        e.setId(d.getId());
        e.setTenantId(d.getTenantId());
        e.setBranchId(d.getBranchId());
        e.setName(d.getName());
        e.setPhone(d.getPhone());
        e.setEmail(d.getEmail());
        e.setLicenseNumber(d.getLicenseNumber());
        e.setVehicleType(d.getVehicleType());
        e.setVehiclePlate(d.getVehiclePlate());
        e.setStatus(d.getStatus());
        e.setRating(d.getRating());
        e.setTotalDeliveries(d.getTotalDeliveries());
        e.setActive(d.isActive());
        e.setCurrentLat(d.getCurrentLat());
        e.setCurrentLng(d.getCurrentLng());
        e.setLastLocationAt(d.getLastLocationAt());
        e.setCreatedAt(d.getCreatedAt());
        e.setUpdatedAt(Instant.now());
        return e;
    }

    private Driver toDomain(DriverEntity e) {
        return Driver.builder()
                .id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId())
                .name(e.getName()).phone(e.getPhone()).email(e.getEmail())
                .licenseNumber(e.getLicenseNumber()).vehicleType(e.getVehicleType())
                .vehiclePlate(e.getVehiclePlate()).status(e.getStatus())
                .rating(e.getRating()).totalDeliveries(e.getTotalDeliveries())
                .isActive(e.isActive()).currentLat(e.getCurrentLat())
                .currentLng(e.getCurrentLng()).lastLocationAt(e.getLastLocationAt())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }
}

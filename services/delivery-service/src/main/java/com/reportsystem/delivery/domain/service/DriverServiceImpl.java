package com.reportsystem.delivery.domain.service;

import com.reportsystem.delivery.domain.model.Driver;
import com.reportsystem.delivery.domain.port.inbound.DriverService;
import com.reportsystem.delivery.domain.port.outbound.DriverRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DriverServiceImpl implements DriverService {

    private final DriverRepository driverRepository;

    public DriverServiceImpl(DriverRepository driverRepository) {
        this.driverRepository = driverRepository;
    }

    @Override
    public Driver createDriver(UUID tenantId, UUID branchId, String name, String phone, String email) {
        Driver driver = Driver.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId)
                .name(name).phone(phone).email(email)
                .status("available").rating(BigDecimal.ZERO).totalDeliveries(0)
                .isActive(true).createdAt(Instant.now()).build();
        return driverRepository.save(driver);
    }

    @Override
    public Optional<Driver> getDriverById(UUID id) {
        return driverRepository.findById(id);
    }

    @Override
    public List<Driver> getDriversByTenant(UUID tenantId) {
        return driverRepository.findByTenantId(tenantId);
    }

    @Override
    public List<Driver> getDriversByTenantAndBranch(UUID tenantId, UUID branchId) {
        return driverRepository.findByTenantIdAndBranchId(tenantId, branchId);
    }

    @Override
    public Driver updateStatus(UUID id, String status) {
        Driver existing = driverRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Driver not found: " + id));
        Driver updated = Driver.builder()
                .id(existing.getId()).tenantId(existing.getTenantId()).branchId(existing.getBranchId())
                .name(existing.getName()).phone(existing.getPhone()).email(existing.getEmail())
                .licenseNumber(existing.getLicenseNumber()).vehicleType(existing.getVehicleType())
                .vehiclePlate(existing.getVehiclePlate()).status(status)
                .rating(existing.getRating()).totalDeliveries(existing.getTotalDeliveries())
                .isActive(existing.isActive()).currentLat(existing.getCurrentLat())
                .currentLng(existing.getCurrentLng()).lastLocationAt(existing.getLastLocationAt())
                .createdAt(existing.getCreatedAt()).updatedAt(Instant.now())
                .build();
        return driverRepository.save(updated);
    }
}

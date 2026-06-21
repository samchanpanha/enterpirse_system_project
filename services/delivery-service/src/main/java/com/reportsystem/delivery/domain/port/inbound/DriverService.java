package com.reportsystem.delivery.domain.port.inbound;

import com.reportsystem.delivery.domain.model.Driver;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverService {
    Driver createDriver(UUID tenantId, UUID branchId, String name, String phone, String email);
    Optional<Driver> getDriverById(UUID id);
    List<Driver> getDriversByTenant(UUID tenantId);
    List<Driver> getDriversByTenantAndBranch(UUID tenantId, UUID branchId);
    Driver updateStatus(UUID id, String status);
}

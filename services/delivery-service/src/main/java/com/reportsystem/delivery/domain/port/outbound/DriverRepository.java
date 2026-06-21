package com.reportsystem.delivery.domain.port.outbound;

import com.reportsystem.delivery.domain.model.Driver;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverRepository {
    Driver save(Driver driver);
    Optional<Driver> findById(UUID id);
    List<Driver> findByTenantId(UUID tenantId);
    List<Driver> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

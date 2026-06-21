package com.reportsystem.delivery.domain.port.outbound;

import com.reportsystem.delivery.domain.model.DriverPayout;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DriverPayoutRepository {
    DriverPayout save(DriverPayout payout);
    Optional<DriverPayout> findById(UUID id);
    List<DriverPayout> findByTenantId(UUID tenantId);
    List<DriverPayout> findByDriverId(UUID driverId);
}

package com.reportsystem.delivery.domain.port.inbound;

import com.reportsystem.delivery.domain.model.DriverPayout;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PayoutService {
    DriverPayout createPayout(UUID tenantId, UUID driverId, java.time.LocalDate periodStart, java.time.LocalDate periodEnd);
    Optional<DriverPayout> getPayoutById(UUID id);
    List<DriverPayout> getPayoutsByTenant(UUID tenantId);
    List<DriverPayout> getPayoutsByDriver(UUID driverId);
    DriverPayout updateStatus(UUID id, String status);
}

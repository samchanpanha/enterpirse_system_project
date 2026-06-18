package com.reportsystem.property.domain.port.inbound;

import com.reportsystem.property.domain.model.Lease;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaseUseCase {
    Lease createLease(UUID tenantId, UUID branchId, UUID unitId, String tenantName, String tenantPhone,
                      java.time.LocalDate startDate, java.time.LocalDate endDate,
                      java.math.BigDecimal rentAmount, java.math.BigDecimal depositAmount);
    Optional<Lease> getLeaseById(UUID id);
    List<Lease> getLeasesByUnit(UUID unitId);
    List<Lease> getActiveLeasesByTenant(UUID tenantId);
    Lease terminateLease(UUID id);
    Lease renewLease(UUID id, java.time.LocalDate newEndDate, java.math.BigDecimal newRent);
}

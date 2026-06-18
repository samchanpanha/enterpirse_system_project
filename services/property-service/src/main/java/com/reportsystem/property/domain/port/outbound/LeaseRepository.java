package com.reportsystem.property.domain.port.outbound;

import com.reportsystem.property.domain.model.Lease;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeaseRepository {
    Lease save(Lease lease);
    Optional<Lease> findById(UUID id);
    List<Lease> findByUnitId(UUID unitId);
    List<Lease> findByTenantId(UUID tenantId);
    List<Lease> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    List<Lease> findActiveByTenantId(UUID tenantId);
    boolean hasOverlappingLease(UUID unitId, java.time.LocalDate startDate, java.time.LocalDate endDate);
}

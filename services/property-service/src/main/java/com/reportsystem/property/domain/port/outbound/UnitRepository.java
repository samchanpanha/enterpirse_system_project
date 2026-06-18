package com.reportsystem.property.domain.port.outbound;

import com.reportsystem.property.domain.model.Unit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UnitRepository {
    Unit save(Unit unit);
    Optional<Unit> findById(UUID id);
    List<Unit> findByPropertyId(UUID propertyId);
    List<Unit> findByTenantId(UUID tenantId);
    List<Unit> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    void deleteById(UUID id);
}

package com.reportsystem.realty.domain.port.outbound;

import com.reportsystem.realty.domain.model.Resident;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResidentRepository {
    Resident save(Resident resident);
    Optional<Resident> findById(UUID id);
    List<Resident> findByTenantId(UUID tenantId);
    List<Resident> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    List<Resident> findByPropertyId(UUID propertyId);
}

package com.reportsystem.realty.domain.port.inbound;

import com.reportsystem.realty.domain.model.Resident;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ResidentService {
    Resident createResident(UUID tenantId, UUID branchId, UUID propertyId, String name);
    Optional<Resident> getResidentById(UUID id);
    List<Resident> getResidentsByTenant(UUID tenantId);
    List<Resident> getResidentsByTenantAndBranch(UUID tenantId, UUID branchId);
    List<Resident> getResidentsByProperty(UUID propertyId);
    Resident updateResident(Resident resident);
}

package com.reportsystem.realty.domain.port.outbound;

import com.reportsystem.realty.domain.model.Visitor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VisitorRepository {
    Visitor save(Visitor visitor);
    Optional<Visitor> findById(UUID id);
    List<Visitor> findByTenantId(UUID tenantId);
    List<Visitor> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    List<Visitor> findByPropertyId(UUID propertyId);
}

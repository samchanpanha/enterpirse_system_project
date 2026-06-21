package com.reportsystem.realty.domain.port.inbound;

import com.reportsystem.realty.domain.model.Visitor;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VisitorService {
    Visitor createVisitor(UUID tenantId, UUID branchId, UUID propertyId, String name);
    Optional<Visitor> getVisitorById(UUID id);
    List<Visitor> getVisitorsByTenant(UUID tenantId);
    List<Visitor> getVisitorsByTenantAndBranch(UUID tenantId, UUID branchId);
    List<Visitor> getVisitorsByProperty(UUID propertyId);
    Visitor updateVisitor(Visitor visitor);
}

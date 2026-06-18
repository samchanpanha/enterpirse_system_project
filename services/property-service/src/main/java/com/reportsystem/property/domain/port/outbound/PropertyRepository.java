package com.reportsystem.property.domain.port.outbound;

import com.reportsystem.property.domain.model.Property;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyRepository {
    Property save(Property property);
    Optional<Property> findById(UUID id);
    List<Property> findByTenantId(UUID tenantId);
    List<Property> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    void deleteById(UUID id);
}

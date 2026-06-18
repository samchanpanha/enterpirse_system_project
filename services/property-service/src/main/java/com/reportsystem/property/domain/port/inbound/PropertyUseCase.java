package com.reportsystem.property.domain.port.inbound;

import com.reportsystem.property.domain.model.Property;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PropertyUseCase {
    Property createProperty(UUID tenantId, UUID branchId, String name, String type, String address, String city, String district);
    Optional<Property> getPropertyById(UUID id);
    List<Property> getPropertiesByTenant(UUID tenantId);
    List<Property> getPropertiesByTenantAndBranch(UUID tenantId, UUID branchId);
    Property updateProperty(UUID id, String name, String type, String address, String city, String district, String status);
    void deleteProperty(UUID id);
}

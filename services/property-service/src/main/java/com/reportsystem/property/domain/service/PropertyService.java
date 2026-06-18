package com.reportsystem.property.domain.service;

import com.reportsystem.property.domain.model.Property;
import com.reportsystem.property.domain.port.inbound.PropertyUseCase;
import com.reportsystem.property.domain.port.outbound.PropertyRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PropertyService implements PropertyUseCase {

    private final PropertyRepository propertyRepository;

    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    @Override
    public Property createProperty(UUID tenantId, UUID branchId, String name, String type, String address, String city, String district) {
        Property property = Property.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .branchId(branchId)
                .name(name)
                .type(type)
                .address(address)
                .city(city)
                .district(district)
                .totalUnits(0)
                .status("active")
                .createdAt(Instant.now())
                .build();
        return propertyRepository.save(property);
    }

    @Override
    public Optional<Property> getPropertyById(UUID id) {
        return propertyRepository.findById(id);
    }

    @Override
    public List<Property> getPropertiesByTenant(UUID tenantId) {
        return propertyRepository.findByTenantId(tenantId);
    }

    @Override
    public List<Property> getPropertiesByTenantAndBranch(UUID tenantId, UUID branchId) {
        return propertyRepository.findByTenantIdAndBranchId(tenantId, branchId);
    }

    @Override
    public Property updateProperty(UUID id, String name, String type, String address, String city, String district, String status) {
        Property existing = propertyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Property not found: " + id));
        Property updated = Property.builder()
                .id(existing.getId()).tenantId(existing.getTenantId())
                .name(name != null ? name : existing.getName())
                .type(type != null ? type : existing.getType())
                .address(address != null ? address : existing.getAddress())
                .city(city != null ? city : existing.getCity())
                .district(district != null ? district : existing.getDistrict())
                .lat(existing.getLat()).lng(existing.getLng())
                .totalUnits(existing.getTotalUnits())
                .status(status != null ? status : existing.getStatus())
                .ownerName(existing.getOwnerName()).ownerPhone(existing.getOwnerPhone())
                .notes(existing.getNotes())
                .createdAt(existing.getCreatedAt()).updatedAt(Instant.now())
                .build();
        return propertyRepository.save(updated);
    }

    @Override
    public void deleteProperty(UUID id) {
        propertyRepository.deleteById(id);
    }
}

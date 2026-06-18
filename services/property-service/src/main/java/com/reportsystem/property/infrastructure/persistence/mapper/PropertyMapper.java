package com.reportsystem.property.infrastructure.persistence.mapper;

import com.reportsystem.property.domain.model.Property;
import com.reportsystem.property.infrastructure.persistence.entity.PropertyEntity;
import org.springframework.stereotype.Component;

@Component
public class PropertyMapper {
    public PropertyEntity toEntity(Property domain) {
        return PropertyEntity.builder().id(domain.getId()).tenantId(domain.getTenantId()).branchId(domain.getBranchId())
                .name(domain.getName()).type(domain.getType()).address(domain.getAddress())
                .city(domain.getCity()).district(domain.getDistrict()).lat(domain.getLat()).lng(domain.getLng())
                .totalUnits(domain.getTotalUnits()).status(domain.getStatus())
                .ownerName(domain.getOwnerName()).ownerPhone(domain.getOwnerPhone()).notes(domain.getNotes())
                .createdAt(domain.getCreatedAt()).updatedAt(domain.getUpdatedAt()).build();
    }
    public Property toDomain(PropertyEntity entity) {
        return Property.builder().id(entity.getId()).tenantId(entity.getTenantId()).branchId(entity.getBranchId())
                .name(entity.getName()).type(entity.getType()).address(entity.getAddress())
                .city(entity.getCity()).district(entity.getDistrict()).lat(entity.getLat()).lng(entity.getLng())
                .totalUnits(entity.getTotalUnits()).status(entity.getStatus())
                .ownerName(entity.getOwnerName()).ownerPhone(entity.getOwnerPhone()).notes(entity.getNotes())
                .createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt()).build();
    }
}

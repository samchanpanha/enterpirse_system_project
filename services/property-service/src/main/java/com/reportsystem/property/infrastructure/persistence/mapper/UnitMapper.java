package com.reportsystem.property.infrastructure.persistence.mapper;

import com.reportsystem.property.domain.model.Unit;
import com.reportsystem.property.infrastructure.persistence.entity.UnitEntity;
import org.springframework.stereotype.Component;

@Component
public class UnitMapper {
    public UnitEntity toEntity(Unit domain) {
        return UnitEntity.builder().id(domain.getId()).tenantId(domain.getTenantId()).branchId(domain.getBranchId())
                .propertyId(domain.getPropertyId()).label(domain.getLabel()).floor(domain.getFloor())
                .bedrooms(domain.getBedrooms()).bathrooms(domain.getBathrooms()).areaSqm(domain.getAreaSqm())
                .rentAmount(domain.getRentAmount()).depositAmount(domain.getDepositAmount())
                .currency(domain.getCurrency()).status(domain.getStatus()).type(domain.getType())
                .amenities(domain.getAmenities()).images(domain.getImages())
                .createdAt(domain.getCreatedAt()).updatedAt(domain.getUpdatedAt()).build();
    }
    public Unit toDomain(UnitEntity entity) {
        return Unit.builder().id(entity.getId()).tenantId(entity.getTenantId()).branchId(entity.getBranchId())
                .propertyId(entity.getPropertyId()).label(entity.getLabel()).floor(entity.getFloor())
                .bedrooms(entity.getBedrooms()).bathrooms(entity.getBathrooms()).areaSqm(entity.getAreaSqm())
                .rentAmount(entity.getRentAmount()).depositAmount(entity.getDepositAmount())
                .currency(entity.getCurrency()).status(entity.getStatus()).type(entity.getType())
                .amenities(entity.getAmenities()).images(entity.getImages())
                .createdAt(entity.getCreatedAt()).updatedAt(entity.getUpdatedAt()).build();
    }
}

package com.reportsystem.realty.infrastructure.persistence.repository;

import com.reportsystem.realty.infrastructure.persistence.entity.PropertyValuationEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPropertyValuationRepository extends JpaRepository<PropertyValuationEntity, UUID> {
    List<PropertyValuationEntity> findByTenantId(UUID tenantId);
    List<PropertyValuationEntity> findByPropertyId(UUID propertyId);
}

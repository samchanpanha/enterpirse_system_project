package com.reportsystem.property.infrastructure.persistence.repository;

import com.reportsystem.property.infrastructure.persistence.entity.UnitEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUnitRepository extends JpaRepository<UnitEntity, UUID> {
    List<UnitEntity> findByPropertyId(UUID propertyId);
    List<UnitEntity> findByTenantId(UUID tenantId);
    List<UnitEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

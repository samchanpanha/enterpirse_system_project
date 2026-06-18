package com.reportsystem.property.infrastructure.persistence.repository;

import com.reportsystem.property.infrastructure.persistence.entity.PropertyEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPropertyRepository extends JpaRepository<PropertyEntity, UUID> {
    List<PropertyEntity> findByTenantId(UUID tenantId);
    List<PropertyEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

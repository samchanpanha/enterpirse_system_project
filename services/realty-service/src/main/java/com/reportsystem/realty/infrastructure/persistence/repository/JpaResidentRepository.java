package com.reportsystem.realty.infrastructure.persistence.repository;

import com.reportsystem.realty.infrastructure.persistence.entity.ResidentEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaResidentRepository extends JpaRepository<ResidentEntity, UUID> {
    List<ResidentEntity> findByTenantId(UUID tenantId);
    List<ResidentEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    List<ResidentEntity> findByPropertyId(UUID propertyId);
}

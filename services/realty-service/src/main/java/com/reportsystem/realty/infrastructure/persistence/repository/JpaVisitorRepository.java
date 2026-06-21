package com.reportsystem.realty.infrastructure.persistence.repository;

import com.reportsystem.realty.infrastructure.persistence.entity.VisitorEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaVisitorRepository extends JpaRepository<VisitorEntity, UUID> {
    List<VisitorEntity> findByTenantId(UUID tenantId);
    List<VisitorEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    List<VisitorEntity> findByPropertyId(UUID propertyId);
}

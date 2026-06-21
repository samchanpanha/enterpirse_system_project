package com.reportsystem.realty.infrastructure.persistence.repository;

import com.reportsystem.realty.infrastructure.persistence.entity.ParcelEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaParcelRepository extends JpaRepository<ParcelEntity, UUID> {
    List<ParcelEntity> findByTenantId(UUID tenantId);
    List<ParcelEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

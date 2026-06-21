package com.reportsystem.realty.infrastructure.persistence.repository;

import com.reportsystem.realty.infrastructure.persistence.entity.ListingEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaListingRepository extends JpaRepository<ListingEntity, UUID> {
    List<ListingEntity> findByTenantId(UUID tenantId);
    List<ListingEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

package com.reportsystem.realty.infrastructure.persistence.repository;

import com.reportsystem.realty.infrastructure.persistence.entity.TourEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaTourRepository extends JpaRepository<TourEntity, UUID> {
    List<TourEntity> findByTenantId(UUID tenantId);
    List<TourEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    List<TourEntity> findByListingId(UUID listingId);
}

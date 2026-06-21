package com.reportsystem.realty.infrastructure.persistence.repository;

import com.reportsystem.realty.infrastructure.persistence.entity.OfferEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOfferRepository extends JpaRepository<OfferEntity, UUID> {
    List<OfferEntity> findByTenantId(UUID tenantId);
    List<OfferEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    List<OfferEntity> findByListingId(UUID listingId);
}

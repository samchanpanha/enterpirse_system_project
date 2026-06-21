package com.reportsystem.realty.infrastructure.persistence.repository;

import com.reportsystem.realty.infrastructure.persistence.entity.LeadEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaLeadRepository extends JpaRepository<LeadEntity, UUID> {
    List<LeadEntity> findByTenantId(UUID tenantId);
    List<LeadEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    List<LeadEntity> findByListingId(UUID listingId);
}

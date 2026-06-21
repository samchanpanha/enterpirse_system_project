package com.reportsystem.realty.domain.port.outbound;

import com.reportsystem.realty.domain.model.Lead;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeadRepository {
    Lead save(Lead lead);
    Optional<Lead> findById(UUID id);
    List<Lead> findByTenantId(UUID tenantId);
    List<Lead> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    List<Lead> findByListingId(UUID listingId);
}

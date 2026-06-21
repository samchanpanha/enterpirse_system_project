package com.reportsystem.realty.domain.port.outbound;

import com.reportsystem.realty.domain.model.Listing;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListingRepository {
    Listing save(Listing listing);
    Optional<Listing> findById(UUID id);
    List<Listing> findByTenantId(UUID tenantId);
    List<Listing> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    void deleteById(UUID id);
}

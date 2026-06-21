package com.reportsystem.realty.domain.port.outbound;

import com.reportsystem.realty.domain.model.Offer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OfferRepository {
    Offer save(Offer offer);
    Optional<Offer> findById(UUID id);
    List<Offer> findByTenantId(UUID tenantId);
    List<Offer> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    List<Offer> findByListingId(UUID listingId);
}

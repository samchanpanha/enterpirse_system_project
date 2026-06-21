package com.reportsystem.realty.domain.port.inbound;

import com.reportsystem.realty.domain.model.Offer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OfferService {
    Offer createOffer(UUID tenantId, UUID branchId, UUID listingId, UUID buyerId, java.math.BigDecimal amount);
    Optional<Offer> getOfferById(UUID id);
    List<Offer> getOffersByTenant(UUID tenantId);
    List<Offer> getOffersByTenantAndBranch(UUID tenantId, UUID branchId);
    List<Offer> getOffersByListing(UUID listingId);
    Offer updateOffer(Offer offer);
}

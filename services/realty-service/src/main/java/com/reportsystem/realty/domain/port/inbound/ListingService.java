package com.reportsystem.realty.domain.port.inbound;

import com.reportsystem.realty.domain.model.Listing;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListingService {
    Listing createListing(UUID tenantId, UUID branchId, String title, String propertyType);
    Optional<Listing> getListingById(UUID id);
    List<Listing> getListingsByTenant(UUID tenantId);
    List<Listing> getListingsByTenantAndBranch(UUID tenantId, UUID branchId);
    Listing updateListing(Listing listing);
    void deleteListing(UUID id);
}

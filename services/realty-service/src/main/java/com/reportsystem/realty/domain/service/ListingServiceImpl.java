package com.reportsystem.realty.domain.service;

import com.reportsystem.realty.domain.model.Listing;
import com.reportsystem.realty.domain.port.inbound.ListingService;
import com.reportsystem.realty.domain.port.outbound.ListingRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ListingServiceImpl implements ListingService {

    private final ListingRepository listingRepository;

    public ListingServiceImpl(ListingRepository listingRepository) {
        this.listingRepository = listingRepository;
    }

    @Override
    public Listing createListing(UUID tenantId, UUID branchId, String title, String propertyType) {
        Listing listing = Listing.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId).title(title)
                .propertyType(propertyType).status("draft").currency("USD")
                .featured(false).published(false).viewCount(0)
                .createdAt(Instant.now()).build();
        return listingRepository.save(listing);
    }

    @Override
    public Optional<Listing> getListingById(UUID id) {
        return listingRepository.findById(id);
    }

    @Override
    public List<Listing> getListingsByTenant(UUID tenantId) {
        return listingRepository.findByTenantId(tenantId);
    }

    @Override
    public List<Listing> getListingsByTenantAndBranch(UUID tenantId, UUID branchId) {
        return listingRepository.findByTenantIdAndBranchId(tenantId, branchId);
    }

    @Override
    public Listing updateListing(Listing listing) {
        return listingRepository.save(listing);
    }

    @Override
    public void deleteListing(UUID id) {
        listingRepository.deleteById(id);
    }
}

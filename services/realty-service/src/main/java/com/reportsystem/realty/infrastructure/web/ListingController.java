package com.reportsystem.realty.infrastructure.web;

import com.reportsystem.realty.domain.model.Listing;
import com.reportsystem.realty.domain.port.inbound.ListingService;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/listings")
public class ListingController {

    private final ListingService listingService;
    public ListingController(ListingService listingService) { this.listingService = listingService; }

    @PostMapping
    public ResponseEntity<Listing> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                          @RequestBody Map<String, String> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(
                listingService.createListing(UUID.fromString(b.get("tenantId")), bid, b.get("title"), b.get("propertyType")));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Listing> get(@PathVariable UUID id) {
        return listingService.getListingById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-tenant/{tenantId}")
    public ResponseEntity<List<Listing>> getByTenant(@PathVariable UUID tenantId,
                                                     @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(listingService.getListingsByTenantAndBranch(tenantId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(listingService.getListingsByTenant(tenantId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Listing> update(@PathVariable UUID id, @RequestBody Listing listing) {
        return listingService.getListingById(id).map(existing -> {
            Listing updated = Listing.builder()
                    .id(existing.getId()).tenantId(existing.getTenantId()).branchId(existing.getBranchId())
                    .agentId(listing.getAgentId()).title(listing.getTitle()).titleKh(listing.getTitleKh())
                    .description(listing.getDescription()).descriptionKh(listing.getDescriptionKh())
                    .propertyType(listing.getPropertyType()).listingType(listing.getListingType())
                    .status(listing.getStatus()).price(listing.getPrice()).currency(listing.getCurrency())
                    .areaSqm(listing.getAreaSqm()).bedrooms(listing.getBedrooms()).bathrooms(listing.getBathrooms())
                    .floors(listing.getFloors()).yearBuilt(listing.getYearBuilt()).address(listing.getAddress())
                    .city(listing.getCity()).district(listing.getDistrict()).province(listing.getProvince())
                    .lat(listing.getLat()).lng(listing.getLng()).features(listing.getFeatures())
                    .featured(listing.isFeatured()).published(listing.isPublished()).viewCount(listing.getViewCount())
                    .createdAt(existing.getCreatedAt()).updatedAt(java.time.Instant.now())
                    .build();
            return ResponseEntity.ok(listingService.updateListing(updated));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        listingService.deleteListing(id);
        return ResponseEntity.noContent().build();
    }
}

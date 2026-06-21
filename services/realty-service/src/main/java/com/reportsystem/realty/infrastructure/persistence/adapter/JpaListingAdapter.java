package com.reportsystem.realty.infrastructure.persistence.adapter;

import com.reportsystem.realty.domain.model.Listing;
import com.reportsystem.realty.domain.port.outbound.ListingRepository;
import com.reportsystem.realty.infrastructure.persistence.entity.ListingEntity;
import com.reportsystem.realty.infrastructure.persistence.repository.JpaListingRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaListingAdapter implements ListingRepository {

    private final JpaListingRepository repo;
    public JpaListingAdapter(JpaListingRepository repo) { this.repo = repo; }

    @Override
    public Listing save(Listing listing) {
        ListingEntity e = new ListingEntity();
        e.setId(listing.getId());
        e.setTenantId(listing.getTenantId());
        e.setBranchId(listing.getBranchId());
        e.setAgentId(listing.getAgentId());
        e.setTitle(listing.getTitle());
        e.setTitleKh(listing.getTitleKh());
        e.setDescription(listing.getDescription());
        e.setDescriptionKh(listing.getDescriptionKh());
        e.setPropertyType(listing.getPropertyType());
        e.setListingType(listing.getListingType());
        e.setStatus(listing.getStatus());
        e.setPrice(listing.getPrice());
        e.setCurrency(listing.getCurrency() != null ? listing.getCurrency() : "USD");
        e.setAreaSqm(listing.getAreaSqm());
        e.setBedrooms(listing.getBedrooms());
        e.setBathrooms(listing.getBathrooms());
        e.setFloors(listing.getFloors());
        e.setYearBuilt(listing.getYearBuilt());
        e.setAddress(listing.getAddress());
        e.setCity(listing.getCity());
        e.setDistrict(listing.getDistrict());
        e.setProvince(listing.getProvince());
        e.setLat(listing.getLat());
        e.setLng(listing.getLng());
        e.setFeatures(listing.getFeatures() != null ? listing.getFeatures() : "[]");
        e.setFeatured(listing.isFeatured());
        e.setPublished(listing.isPublished());
        e.setViewCount(listing.getViewCount());
        e.setCreatedAt(Instant.now());
        ListingEntity saved = repo.save(e);
        return toDomain(saved);
    }

    @Override
    public Optional<Listing> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Listing> findByTenantId(UUID tenantId) {
        return repo.findByTenantId(tenantId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Listing> findByTenantIdAndBranchId(UUID tenantId, UUID branchId) {
        return repo.findByTenantIdAndBranchId(tenantId, branchId).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        repo.deleteById(id);
    }

    private Listing toDomain(ListingEntity e) {
        return Listing.builder()
                .id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId())
                .agentId(e.getAgentId()).title(e.getTitle()).titleKh(e.getTitleKh())
                .description(e.getDescription()).descriptionKh(e.getDescriptionKh())
                .propertyType(e.getPropertyType()).listingType(e.getListingType())
                .status(e.getStatus()).price(e.getPrice()).currency(e.getCurrency())
                .areaSqm(e.getAreaSqm()).bedrooms(e.getBedrooms()).bathrooms(e.getBathrooms())
                .floors(e.getFloors()).yearBuilt(e.getYearBuilt()).address(e.getAddress())
                .city(e.getCity()).district(e.getDistrict()).province(e.getProvince())
                .lat(e.getLat()).lng(e.getLng()).features(e.getFeatures())
                .featured(e.isFeatured()).published(e.isPublished()).viewCount(e.getViewCount())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }
}

package com.reportsystem.realty.infrastructure.persistence.adapter;

import com.reportsystem.realty.domain.model.ListingImage;
import com.reportsystem.realty.domain.port.outbound.ListingImageRepository;
import com.reportsystem.realty.infrastructure.persistence.entity.ListingImageEntity;
import com.reportsystem.realty.infrastructure.persistence.repository.JpaListingImageRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaListingImageAdapter implements ListingImageRepository {

    private final JpaListingImageRepository repo;
    public JpaListingImageAdapter(JpaListingImageRepository repo) { this.repo = repo; }

    @Override
    public ListingImage save(ListingImage image) {
        ListingImageEntity e = new ListingImageEntity();
        e.setId(image.getId());
        e.setListingId(image.getListingId());
        e.setImageUrl(image.getImageUrl());
        e.setPrimary(image.isPrimary());
        e.setSortOrder(image.getSortOrder());
        e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e));
    }

    @Override
    public Optional<ListingImage> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<ListingImage> findByListingId(UUID listingId) {
        return repo.findByListingId(listingId).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        repo.deleteById(id);
    }

    private ListingImage toDomain(ListingImageEntity e) {
        return ListingImage.builder()
                .id(e.getId()).listingId(e.getListingId())
                .imageUrl(e.getImageUrl()).primary(e.isPrimary())
                .sortOrder(e.getSortOrder()).createdAt(e.getCreatedAt())
                .build();
    }
}

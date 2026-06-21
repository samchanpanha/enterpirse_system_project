package com.reportsystem.realty.infrastructure.persistence.repository;

import com.reportsystem.realty.infrastructure.persistence.entity.ListingImageEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaListingImageRepository extends JpaRepository<ListingImageEntity, UUID> {
    List<ListingImageEntity> findByListingId(UUID listingId);
}

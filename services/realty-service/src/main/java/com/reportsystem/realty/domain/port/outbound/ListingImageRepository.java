package com.reportsystem.realty.domain.port.outbound;

import com.reportsystem.realty.domain.model.ListingImage;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListingImageRepository {
    ListingImage save(ListingImage image);
    Optional<ListingImage> findById(UUID id);
    List<ListingImage> findByListingId(UUID listingId);
    void deleteById(UUID id);
}

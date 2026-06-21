package com.reportsystem.realty.domain.port.outbound;

import com.reportsystem.realty.domain.model.Tour;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TourRepository {
    Tour save(Tour tour);
    Optional<Tour> findById(UUID id);
    List<Tour> findByTenantId(UUID tenantId);
    List<Tour> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    List<Tour> findByListingId(UUID listingId);
}

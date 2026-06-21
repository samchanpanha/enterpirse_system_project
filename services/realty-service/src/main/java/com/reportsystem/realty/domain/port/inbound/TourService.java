package com.reportsystem.realty.domain.port.inbound;

import com.reportsystem.realty.domain.model.Tour;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TourService {
    Tour createTour(UUID tenantId, UUID branchId, UUID listingId, UUID agentId, UUID leadId);
    Optional<Tour> getTourById(UUID id);
    List<Tour> getToursByTenant(UUID tenantId);
    List<Tour> getToursByTenantAndBranch(UUID tenantId, UUID branchId);
    List<Tour> getToursByListing(UUID listingId);
    Tour updateTour(Tour tour);
}

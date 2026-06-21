package com.reportsystem.realty.domain.service;

import com.reportsystem.realty.domain.model.Tour;
import com.reportsystem.realty.domain.port.inbound.TourService;
import com.reportsystem.realty.domain.port.outbound.TourRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class TourServiceImpl implements TourService {

    private final TourRepository tourRepository;

    public TourServiceImpl(TourRepository tourRepository) {
        this.tourRepository = tourRepository;
    }

    @Override
    public Tour createTour(UUID tenantId, UUID branchId, UUID listingId, UUID agentId, UUID leadId) {
        Tour tour = Tour.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId)
                .listingId(listingId).agentId(agentId).leadId(leadId)
                .status("scheduled")
                .createdAt(Instant.now()).build();
        return tourRepository.save(tour);
    }

    @Override
    public Optional<Tour> getTourById(UUID id) {
        return tourRepository.findById(id);
    }

    @Override
    public List<Tour> getToursByTenant(UUID tenantId) {
        return tourRepository.findByTenantId(tenantId);
    }

    @Override
    public List<Tour> getToursByTenantAndBranch(UUID tenantId, UUID branchId) {
        return tourRepository.findByTenantIdAndBranchId(tenantId, branchId);
    }

    @Override
    public List<Tour> getToursByListing(UUID listingId) {
        return tourRepository.findByListingId(listingId);
    }

    @Override
    public Tour updateTour(Tour tour) {
        return tourRepository.save(tour);
    }
}

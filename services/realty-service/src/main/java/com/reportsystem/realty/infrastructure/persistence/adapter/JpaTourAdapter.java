package com.reportsystem.realty.infrastructure.persistence.adapter;

import com.reportsystem.realty.domain.model.Tour;
import com.reportsystem.realty.domain.port.outbound.TourRepository;
import com.reportsystem.realty.infrastructure.persistence.entity.TourEntity;
import com.reportsystem.realty.infrastructure.persistence.repository.JpaTourRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaTourAdapter implements TourRepository {

    private final JpaTourRepository repo;
    public JpaTourAdapter(JpaTourRepository repo) { this.repo = repo; }

    @Override
    public Tour save(Tour tour) {
        TourEntity e = new TourEntity();
        e.setId(tour.getId());
        e.setTenantId(tour.getTenantId());
        e.setBranchId(tour.getBranchId());
        e.setListingId(tour.getListingId());
        e.setAgentId(tour.getAgentId());
        e.setLeadId(tour.getLeadId());
        e.setScheduledAt(tour.getScheduledAt());
        e.setStatus(tour.getStatus() != null ? tour.getStatus() : "scheduled");
        e.setNotes(tour.getNotes());
        e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e));
    }

    @Override
    public Optional<Tour> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Tour> findByTenantId(UUID tenantId) {
        return repo.findByTenantId(tenantId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Tour> findByTenantIdAndBranchId(UUID tenantId, UUID branchId) {
        return repo.findByTenantIdAndBranchId(tenantId, branchId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Tour> findByListingId(UUID listingId) {
        return repo.findByListingId(listingId).stream().map(this::toDomain).toList();
    }

    private Tour toDomain(TourEntity e) {
        return Tour.builder()
                .id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId())
                .listingId(e.getListingId()).agentId(e.getAgentId()).leadId(e.getLeadId())
                .scheduledAt(e.getScheduledAt()).status(e.getStatus()).notes(e.getNotes())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }
}

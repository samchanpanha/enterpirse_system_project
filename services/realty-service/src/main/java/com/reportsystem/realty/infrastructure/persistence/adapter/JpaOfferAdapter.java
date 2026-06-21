package com.reportsystem.realty.infrastructure.persistence.adapter;

import com.reportsystem.realty.domain.model.Offer;
import com.reportsystem.realty.domain.port.outbound.OfferRepository;
import com.reportsystem.realty.infrastructure.persistence.entity.OfferEntity;
import com.reportsystem.realty.infrastructure.persistence.repository.JpaOfferRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaOfferAdapter implements OfferRepository {

    private final JpaOfferRepository repo;
    public JpaOfferAdapter(JpaOfferRepository repo) { this.repo = repo; }

    @Override
    public Offer save(Offer offer) {
        OfferEntity e = new OfferEntity();
        e.setId(offer.getId());
        e.setTenantId(offer.getTenantId());
        e.setBranchId(offer.getBranchId());
        e.setListingId(offer.getListingId());
        e.setBuyerId(offer.getBuyerId());
        e.setAgentId(offer.getAgentId());
        e.setAmount(offer.getAmount());
        e.setCurrency(offer.getCurrency() != null ? offer.getCurrency() : "USD");
        e.setTerms(offer.getTerms());
        e.setStatus(offer.getStatus() != null ? offer.getStatus() : "pending");
        e.setExpiryDate(offer.getExpiryDate());
        e.setCounterAmount(offer.getCounterAmount());
        e.setRejectedReason(offer.getRejectedReason());
        e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e));
    }

    @Override
    public Optional<Offer> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Offer> findByTenantId(UUID tenantId) {
        return repo.findByTenantId(tenantId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Offer> findByTenantIdAndBranchId(UUID tenantId, UUID branchId) {
        return repo.findByTenantIdAndBranchId(tenantId, branchId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Offer> findByListingId(UUID listingId) {
        return repo.findByListingId(listingId).stream().map(this::toDomain).toList();
    }

    private Offer toDomain(OfferEntity e) {
        return Offer.builder()
                .id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId())
                .listingId(e.getListingId()).buyerId(e.getBuyerId()).agentId(e.getAgentId())
                .amount(e.getAmount()).currency(e.getCurrency()).terms(e.getTerms())
                .status(e.getStatus()).expiryDate(e.getExpiryDate())
                .counterAmount(e.getCounterAmount()).rejectedReason(e.getRejectedReason())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }
}

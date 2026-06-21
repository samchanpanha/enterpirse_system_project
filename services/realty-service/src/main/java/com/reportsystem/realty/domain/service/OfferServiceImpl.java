package com.reportsystem.realty.domain.service;

import com.reportsystem.realty.domain.model.Offer;
import com.reportsystem.realty.domain.port.inbound.OfferService;
import com.reportsystem.realty.domain.port.outbound.OfferRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OfferServiceImpl implements OfferService {

    private final OfferRepository offerRepository;

    public OfferServiceImpl(OfferRepository offerRepository) {
        this.offerRepository = offerRepository;
    }

    @Override
    public Offer createOffer(UUID tenantId, UUID branchId, UUID listingId, UUID buyerId, BigDecimal amount) {
        Offer offer = Offer.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId)
                .listingId(listingId).buyerId(buyerId).amount(amount)
                .currency("USD").status("pending")
                .createdAt(Instant.now()).build();
        return offerRepository.save(offer);
    }

    @Override
    public Optional<Offer> getOfferById(UUID id) {
        return offerRepository.findById(id);
    }

    @Override
    public List<Offer> getOffersByTenant(UUID tenantId) {
        return offerRepository.findByTenantId(tenantId);
    }

    @Override
    public List<Offer> getOffersByTenantAndBranch(UUID tenantId, UUID branchId) {
        return offerRepository.findByTenantIdAndBranchId(tenantId, branchId);
    }

    @Override
    public List<Offer> getOffersByListing(UUID listingId) {
        return offerRepository.findByListingId(listingId);
    }

    @Override
    public Offer updateOffer(Offer offer) {
        return offerRepository.save(offer);
    }
}

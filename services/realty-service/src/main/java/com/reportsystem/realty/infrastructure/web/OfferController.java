package com.reportsystem.realty.infrastructure.web;

import com.reportsystem.realty.domain.model.Offer;
import com.reportsystem.realty.domain.port.inbound.OfferService;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/offers")
public class OfferController {

    private final OfferService offerService;
    public OfferController(OfferService offerService) { this.offerService = offerService; }

    @PostMapping
    public ResponseEntity<Offer> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                        @RequestBody Map<String, String> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        UUID listingId = UUID.fromString(b.get("listingId"));
        UUID buyerId = b.containsKey("buyerId") ? UUID.fromString(b.get("buyerId")) : null;
        BigDecimal amount = new BigDecimal(b.get("amount"));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                offerService.createOffer(UUID.fromString(b.get("tenantId")), bid, listingId, buyerId, amount));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Offer> get(@PathVariable UUID id) {
        return offerService.getOfferById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-tenant/{tenantId}")
    public ResponseEntity<List<Offer>> getByTenant(@PathVariable UUID tenantId,
                                                   @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(offerService.getOffersByTenantAndBranch(tenantId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(offerService.getOffersByTenant(tenantId));
    }

    @GetMapping("/by-listing/{listingId}")
    public ResponseEntity<List<Offer>> getByListing(@PathVariable UUID listingId) {
        return ResponseEntity.ok(offerService.getOffersByListing(listingId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Offer> update(@PathVariable UUID id, @RequestBody Offer offer) {
        return offerService.getOfferById(id).map(existing -> {
            Offer updated = Offer.builder()
                    .id(existing.getId()).tenantId(existing.getTenantId()).branchId(existing.getBranchId())
                    .listingId(existing.getListingId()).buyerId(offer.getBuyerId()).agentId(offer.getAgentId())
                    .amount(offer.getAmount()).currency(offer.getCurrency()).terms(offer.getTerms())
                    .status(offer.getStatus()).expiryDate(offer.getExpiryDate())
                    .counterAmount(offer.getCounterAmount()).rejectedReason(offer.getRejectedReason())
                    .createdAt(existing.getCreatedAt()).updatedAt(java.time.Instant.now())
                    .build();
            return ResponseEntity.ok(offerService.updateOffer(updated));
        }).orElse(ResponseEntity.notFound().build());
    }
}

package com.reportsystem.realty.infrastructure.web;

import com.reportsystem.realty.domain.model.Tour;
import com.reportsystem.realty.domain.port.inbound.TourService;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/tours")
public class TourController {

    private final TourService tourService;
    public TourController(TourService tourService) { this.tourService = tourService; }

    @PostMapping
    public ResponseEntity<Tour> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                       @RequestBody Map<String, String> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        UUID listingId = UUID.fromString(b.get("listingId"));
        UUID agentId = b.containsKey("agentId") ? UUID.fromString(b.get("agentId")) : null;
        UUID leadId = b.containsKey("leadId") ? UUID.fromString(b.get("leadId")) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(
                tourService.createTour(UUID.fromString(b.get("tenantId")), bid, listingId, agentId, leadId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tour> get(@PathVariable UUID id) {
        return tourService.getTourById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-tenant/{tenantId}")
    public ResponseEntity<List<Tour>> getByTenant(@PathVariable UUID tenantId,
                                                  @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(tourService.getToursByTenantAndBranch(tenantId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(tourService.getToursByTenant(tenantId));
    }

    @GetMapping("/by-listing/{listingId}")
    public ResponseEntity<List<Tour>> getByListing(@PathVariable UUID listingId) {
        return ResponseEntity.ok(tourService.getToursByListing(listingId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Tour> update(@PathVariable UUID id, @RequestBody Tour tour) {
        return tourService.getTourById(id).map(existing -> {
            Tour updated = Tour.builder()
                    .id(existing.getId()).tenantId(existing.getTenantId()).branchId(existing.getBranchId())
                    .listingId(tour.getListingId()).agentId(tour.getAgentId()).leadId(tour.getLeadId())
                    .scheduledAt(tour.getScheduledAt()).status(tour.getStatus()).notes(tour.getNotes())
                    .createdAt(existing.getCreatedAt()).updatedAt(java.time.Instant.now())
                    .build();
            return ResponseEntity.ok(tourService.updateTour(updated));
        }).orElse(ResponseEntity.notFound().build());
    }
}

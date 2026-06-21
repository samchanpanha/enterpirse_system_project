package com.reportsystem.realty.infrastructure.web;

import com.reportsystem.realty.domain.model.Lead;
import com.reportsystem.realty.domain.port.inbound.LeadService;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/leads")
public class LeadController {

    private final LeadService leadService;
    public LeadController(LeadService leadService) { this.leadService = leadService; }

    @PostMapping
    public ResponseEntity<Lead> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                       @RequestBody Map<String, String> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        UUID listingId = b.containsKey("listingId") ? UUID.fromString(b.get("listingId")) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(
                leadService.createLead(UUID.fromString(b.get("tenantId")), bid, b.get("name"), listingId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lead> get(@PathVariable UUID id) {
        return leadService.getLeadById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-tenant/{tenantId}")
    public ResponseEntity<List<Lead>> getByTenant(@PathVariable UUID tenantId,
                                                  @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(leadService.getLeadsByTenantAndBranch(tenantId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(leadService.getLeadsByTenant(tenantId));
    }

    @GetMapping("/by-listing/{listingId}")
    public ResponseEntity<List<Lead>> getByListing(@PathVariable UUID listingId) {
        return ResponseEntity.ok(leadService.getLeadsByListing(listingId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Lead> update(@PathVariable UUID id, @RequestBody Lead lead) {
        return leadService.getLeadById(id).map(existing -> {
            Lead updated = Lead.builder()
                    .id(existing.getId()).tenantId(existing.getTenantId()).branchId(existing.getBranchId())
                    .listingId(lead.getListingId()).agentId(lead.getAgentId())
                    .name(lead.getName()).phone(lead.getPhone()).email(lead.getEmail())
                    .message(lead.getMessage()).source(lead.getSource()).status(lead.getStatus())
                    .budgetMin(lead.getBudgetMin()).budgetMax(lead.getBudgetMax())
                    .propertyTypePreference(lead.getPropertyTypePreference())
                    .preferredDistrict(lead.getPreferredDistrict()).notes(lead.getNotes())
                    .createdAt(existing.getCreatedAt()).updatedAt(java.time.Instant.now())
                    .build();
            return ResponseEntity.ok(leadService.updateLead(updated));
        }).orElse(ResponseEntity.notFound().build());
    }
}

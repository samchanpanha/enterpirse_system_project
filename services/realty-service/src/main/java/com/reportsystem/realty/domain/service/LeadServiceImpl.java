package com.reportsystem.realty.domain.service;

import com.reportsystem.realty.domain.model.Lead;
import com.reportsystem.realty.domain.port.inbound.LeadService;
import com.reportsystem.realty.domain.port.outbound.LeadRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LeadServiceImpl implements LeadService {

    private final LeadRepository leadRepository;

    public LeadServiceImpl(LeadRepository leadRepository) {
        this.leadRepository = leadRepository;
    }

    @Override
    public Lead createLead(UUID tenantId, UUID branchId, String name, UUID listingId) {
        Lead lead = Lead.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId)
                .name(name).listingId(listingId).status("new")
                .createdAt(Instant.now()).build();
        return leadRepository.save(lead);
    }

    @Override
    public Optional<Lead> getLeadById(UUID id) {
        return leadRepository.findById(id);
    }

    @Override
    public List<Lead> getLeadsByTenant(UUID tenantId) {
        return leadRepository.findByTenantId(tenantId);
    }

    @Override
    public List<Lead> getLeadsByTenantAndBranch(UUID tenantId, UUID branchId) {
        return leadRepository.findByTenantIdAndBranchId(tenantId, branchId);
    }

    @Override
    public List<Lead> getLeadsByListing(UUID listingId) {
        return leadRepository.findByListingId(listingId);
    }

    @Override
    public Lead updateLead(Lead lead) {
        return leadRepository.save(lead);
    }
}

package com.reportsystem.realty.infrastructure.persistence.adapter;

import com.reportsystem.realty.domain.model.Lead;
import com.reportsystem.realty.domain.port.outbound.LeadRepository;
import com.reportsystem.realty.infrastructure.persistence.entity.LeadEntity;
import com.reportsystem.realty.infrastructure.persistence.repository.JpaLeadRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaLeadAdapter implements LeadRepository {

    private final JpaLeadRepository repo;
    public JpaLeadAdapter(JpaLeadRepository repo) { this.repo = repo; }

    @Override
    public Lead save(Lead lead) {
        LeadEntity e = new LeadEntity();
        e.setId(lead.getId());
        e.setTenantId(lead.getTenantId());
        e.setBranchId(lead.getBranchId());
        e.setListingId(lead.getListingId());
        e.setAgentId(lead.getAgentId());
        e.setName(lead.getName());
        e.setPhone(lead.getPhone());
        e.setEmail(lead.getEmail());
        e.setMessage(lead.getMessage());
        e.setSource(lead.getSource());
        e.setStatus(lead.getStatus() != null ? lead.getStatus() : "new");
        e.setBudgetMin(lead.getBudgetMin());
        e.setBudgetMax(lead.getBudgetMax());
        e.setPropertyTypePreference(lead.getPropertyTypePreference());
        e.setPreferredDistrict(lead.getPreferredDistrict());
        e.setNotes(lead.getNotes());
        e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e));
    }

    @Override
    public Optional<Lead> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Lead> findByTenantId(UUID tenantId) {
        return repo.findByTenantId(tenantId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Lead> findByTenantIdAndBranchId(UUID tenantId, UUID branchId) {
        return repo.findByTenantIdAndBranchId(tenantId, branchId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Lead> findByListingId(UUID listingId) {
        return repo.findByListingId(listingId).stream().map(this::toDomain).toList();
    }

    private Lead toDomain(LeadEntity e) {
        return Lead.builder()
                .id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId())
                .listingId(e.getListingId()).agentId(e.getAgentId())
                .name(e.getName()).phone(e.getPhone()).email(e.getEmail())
                .message(e.getMessage()).source(e.getSource()).status(e.getStatus())
                .budgetMin(e.getBudgetMin()).budgetMax(e.getBudgetMax())
                .propertyTypePreference(e.getPropertyTypePreference())
                .preferredDistrict(e.getPreferredDistrict()).notes(e.getNotes())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }
}

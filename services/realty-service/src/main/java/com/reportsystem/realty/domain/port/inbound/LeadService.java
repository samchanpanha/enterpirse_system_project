package com.reportsystem.realty.domain.port.inbound;

import com.reportsystem.realty.domain.model.Lead;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LeadService {
    Lead createLead(UUID tenantId, UUID branchId, String name, UUID listingId);
    Optional<Lead> getLeadById(UUID id);
    List<Lead> getLeadsByTenant(UUID tenantId);
    List<Lead> getLeadsByTenantAndBranch(UUID tenantId, UUID branchId);
    List<Lead> getLeadsByListing(UUID listingId);
    Lead updateLead(Lead lead);
}

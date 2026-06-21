package com.reportsystem.realty.domain.service;

import com.reportsystem.realty.domain.model.Visitor;
import com.reportsystem.realty.domain.port.inbound.VisitorService;
import com.reportsystem.realty.domain.port.outbound.VisitorRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class VisitorServiceImpl implements VisitorService {

    private final VisitorRepository visitorRepository;

    public VisitorServiceImpl(VisitorRepository visitorRepository) {
        this.visitorRepository = visitorRepository;
    }

    @Override
    public Visitor createVisitor(UUID tenantId, UUID branchId, UUID propertyId, String name) {
        Visitor visitor = Visitor.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId)
                .propertyId(propertyId).name(name).status("checked_in")
                .createdAt(Instant.now()).build();
        return visitorRepository.save(visitor);
    }

    @Override
    public Optional<Visitor> getVisitorById(UUID id) {
        return visitorRepository.findById(id);
    }

    @Override
    public List<Visitor> getVisitorsByTenant(UUID tenantId) {
        return visitorRepository.findByTenantId(tenantId);
    }

    @Override
    public List<Visitor> getVisitorsByTenantAndBranch(UUID tenantId, UUID branchId) {
        return visitorRepository.findByTenantIdAndBranchId(tenantId, branchId);
    }

    @Override
    public List<Visitor> getVisitorsByProperty(UUID propertyId) {
        return visitorRepository.findByPropertyId(propertyId);
    }

    @Override
    public Visitor updateVisitor(Visitor visitor) {
        return visitorRepository.save(visitor);
    }
}

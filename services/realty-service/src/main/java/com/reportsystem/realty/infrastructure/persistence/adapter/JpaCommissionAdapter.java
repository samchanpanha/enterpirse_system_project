package com.reportsystem.realty.infrastructure.persistence.adapter;

import com.reportsystem.realty.domain.model.Commission;
import com.reportsystem.realty.domain.port.outbound.CommissionRepository;
import com.reportsystem.realty.infrastructure.persistence.entity.CommissionEntity;
import com.reportsystem.realty.infrastructure.persistence.repository.JpaCommissionRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaCommissionAdapter implements CommissionRepository {

    private final JpaCommissionRepository repo;
    public JpaCommissionAdapter(JpaCommissionRepository repo) { this.repo = repo; }

    @Override
    public Commission save(Commission commission) {
        CommissionEntity e = new CommissionEntity();
        e.setId(commission.getId());
        e.setTenantId(commission.getTenantId());
        e.setBranchId(commission.getBranchId());
        e.setOfferId(commission.getOfferId());
        e.setAgentId(commission.getAgentId());
        e.setAmount(commission.getAmount());
        e.setPercentage(commission.getPercentage());
        e.setStatus(commission.getStatus() != null ? commission.getStatus() : "pending");
        e.setPaidAt(commission.getPaidAt());
        e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e));
    }

    @Override
    public Optional<Commission> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Commission> findByTenantId(UUID tenantId) {
        return repo.findByTenantId(tenantId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Commission> findByOfferId(UUID offerId) {
        return repo.findByOfferId(offerId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Commission> findByAgentId(UUID agentId) {
        return repo.findByAgentId(agentId).stream().map(this::toDomain).toList();
    }

    private Commission toDomain(CommissionEntity e) {
        return Commission.builder()
                .id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId())
                .offerId(e.getOfferId()).agentId(e.getAgentId())
                .amount(e.getAmount()).percentage(e.getPercentage())
                .status(e.getStatus()).paidAt(e.getPaidAt())
                .createdAt(e.getCreatedAt())
                .build();
    }
}

package com.reportsystem.realty.infrastructure.persistence.adapter;

import com.reportsystem.realty.domain.model.Agent;
import com.reportsystem.realty.domain.port.outbound.AgentRepository;
import com.reportsystem.realty.infrastructure.persistence.entity.AgentEntity;
import com.reportsystem.realty.infrastructure.persistence.repository.JpaAgentRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaAgentAdapter implements AgentRepository {

    private final JpaAgentRepository repo;
    public JpaAgentAdapter(JpaAgentRepository repo) { this.repo = repo; }

    @Override
    public Agent save(Agent agent) {
        AgentEntity e = new AgentEntity();
        e.setId(agent.getId());
        e.setTenantId(agent.getTenantId());
        e.setBranchId(agent.getBranchId());
        e.setUserId(agent.getUserId());
        e.setName(agent.getName());
        e.setNameKh(agent.getNameKh());
        e.setPhone(agent.getPhone());
        e.setEmail(agent.getEmail());
        e.setLicenseNumber(agent.getLicenseNumber());
        e.setBio(agent.getBio());
        e.setAvatarUrl(agent.getAvatarUrl());
        e.setRating(agent.getRating());
        e.setTotalSales(agent.getTotalSales());
        e.setTotalListings(agent.getTotalListings());
        e.setActive(agent.isActive());
        e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e));
    }

    @Override
    public Optional<Agent> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Agent> findByTenantId(UUID tenantId) {
        return repo.findByTenantId(tenantId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Agent> findByTenantIdAndBranchId(UUID tenantId, UUID branchId) {
        return repo.findByTenantIdAndBranchId(tenantId, branchId).stream().map(this::toDomain).toList();
    }

    private Agent toDomain(AgentEntity e) {
        return Agent.builder()
                .id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId())
                .userId(e.getUserId()).name(e.getName()).nameKh(e.getNameKh())
                .phone(e.getPhone()).email(e.getEmail())
                .licenseNumber(e.getLicenseNumber()).bio(e.getBio())
                .avatarUrl(e.getAvatarUrl()).rating(e.getRating())
                .totalSales(e.getTotalSales()).totalListings(e.getTotalListings())
                .active(e.isActive())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }
}

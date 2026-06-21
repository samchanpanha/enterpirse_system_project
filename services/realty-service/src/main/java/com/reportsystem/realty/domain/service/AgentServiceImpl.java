package com.reportsystem.realty.domain.service;

import com.reportsystem.realty.domain.model.Agent;
import com.reportsystem.realty.domain.port.inbound.AgentService;
import com.reportsystem.realty.domain.port.outbound.AgentRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AgentServiceImpl implements AgentService {

    private final AgentRepository agentRepository;

    public AgentServiceImpl(AgentRepository agentRepository) {
        this.agentRepository = agentRepository;
    }

    @Override
    public Agent createAgent(UUID tenantId, UUID branchId, String name, String email) {
        Agent agent = Agent.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId)
                .name(name).email(email).active(true)
                .totalSales(0).totalListings(0)
                .createdAt(Instant.now()).build();
        return agentRepository.save(agent);
    }

    @Override
    public Optional<Agent> getAgentById(UUID id) {
        return agentRepository.findById(id);
    }

    @Override
    public List<Agent> getAgentsByTenant(UUID tenantId) {
        return agentRepository.findByTenantId(tenantId);
    }

    @Override
    public List<Agent> getAgentsByTenantAndBranch(UUID tenantId, UUID branchId) {
        return agentRepository.findByTenantIdAndBranchId(tenantId, branchId);
    }

    @Override
    public Agent updateAgent(Agent agent) {
        return agentRepository.save(agent);
    }
}

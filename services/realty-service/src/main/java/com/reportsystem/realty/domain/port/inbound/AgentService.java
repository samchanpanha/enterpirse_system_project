package com.reportsystem.realty.domain.port.inbound;

import com.reportsystem.realty.domain.model.Agent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgentService {
    Agent createAgent(UUID tenantId, UUID branchId, String name, String email);
    Optional<Agent> getAgentById(UUID id);
    List<Agent> getAgentsByTenant(UUID tenantId);
    List<Agent> getAgentsByTenantAndBranch(UUID tenantId, UUID branchId);
    Agent updateAgent(Agent agent);
}

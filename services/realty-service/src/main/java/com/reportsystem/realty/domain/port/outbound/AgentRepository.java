package com.reportsystem.realty.domain.port.outbound;

import com.reportsystem.realty.domain.model.Agent;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AgentRepository {
    Agent save(Agent agent);
    Optional<Agent> findById(UUID id);
    List<Agent> findByTenantId(UUID tenantId);
    List<Agent> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

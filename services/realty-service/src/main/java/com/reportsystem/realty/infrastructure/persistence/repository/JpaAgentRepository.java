package com.reportsystem.realty.infrastructure.persistence.repository;

import com.reportsystem.realty.infrastructure.persistence.entity.AgentEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaAgentRepository extends JpaRepository<AgentEntity, UUID> {
    List<AgentEntity> findByTenantId(UUID tenantId);
    List<AgentEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

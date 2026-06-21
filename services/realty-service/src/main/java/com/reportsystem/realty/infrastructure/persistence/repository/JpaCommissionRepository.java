package com.reportsystem.realty.infrastructure.persistence.repository;

import com.reportsystem.realty.infrastructure.persistence.entity.CommissionEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCommissionRepository extends JpaRepository<CommissionEntity, UUID> {
    List<CommissionEntity> findByTenantId(UUID tenantId);
    List<CommissionEntity> findByOfferId(UUID offerId);
    List<CommissionEntity> findByAgentId(UUID agentId);
}

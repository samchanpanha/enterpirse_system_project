package com.reportsystem.realty.domain.port.outbound;

import com.reportsystem.realty.domain.model.Commission;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommissionRepository {
    Commission save(Commission commission);
    Optional<Commission> findById(UUID id);
    List<Commission> findByTenantId(UUID tenantId);
    List<Commission> findByOfferId(UUID offerId);
    List<Commission> findByAgentId(UUID agentId);
}

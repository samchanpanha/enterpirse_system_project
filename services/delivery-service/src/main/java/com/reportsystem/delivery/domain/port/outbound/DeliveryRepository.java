package com.reportsystem.delivery.domain.port.outbound;

import com.reportsystem.delivery.domain.model.Delivery;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryRepository {
    Delivery save(Delivery delivery);
    Optional<Delivery> findById(UUID id);
    List<Delivery> findByTenantId(UUID tenantId);
    List<Delivery> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

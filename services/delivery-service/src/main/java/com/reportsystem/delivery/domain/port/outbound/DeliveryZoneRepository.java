package com.reportsystem.delivery.domain.port.outbound;

import com.reportsystem.delivery.domain.model.DeliveryZone;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryZoneRepository {
    DeliveryZone save(DeliveryZone zone);
    Optional<DeliveryZone> findById(UUID id);
    List<DeliveryZone> findByTenantId(UUID tenantId);
    List<DeliveryZone> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

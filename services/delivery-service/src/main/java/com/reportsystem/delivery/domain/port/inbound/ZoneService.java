package com.reportsystem.delivery.domain.port.inbound;

import com.reportsystem.delivery.domain.model.DeliveryZone;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ZoneService {
    DeliveryZone createZone(UUID tenantId, UUID branchId, String name, String nameKh);
    Optional<DeliveryZone> getZoneById(UUID id);
    List<DeliveryZone> getZonesByTenant(UUID tenantId);
    List<DeliveryZone> getZonesByTenantAndBranch(UUID tenantId, UUID branchId);
    DeliveryZone updateStatus(UUID id, boolean isActive);
}

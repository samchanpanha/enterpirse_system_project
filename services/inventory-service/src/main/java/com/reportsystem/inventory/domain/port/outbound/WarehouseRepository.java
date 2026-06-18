package com.reportsystem.inventory.domain.port.outbound;

import com.reportsystem.inventory.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseRepository {
    Warehouse save(Warehouse w);
    Optional<Warehouse> findById(UUID id);
    List<Warehouse> findByTenantId(UUID tenantId);
}

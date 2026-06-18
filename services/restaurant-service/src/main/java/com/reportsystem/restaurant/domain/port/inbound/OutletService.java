package com.reportsystem.restaurant.domain.port.inbound;

import com.reportsystem.restaurant.domain.model.Outlet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OutletService {
    Outlet createOutlet(UUID tenantId, UUID branchId, String name);
    Optional<Outlet> getOutletById(UUID id);
    List<Outlet> getOutletsByTenant(UUID tenantId);
    List<Outlet> getOutletsByTenantAndBranch(UUID tenantId, UUID branchId);
}

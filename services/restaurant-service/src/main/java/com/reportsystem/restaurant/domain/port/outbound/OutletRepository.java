package com.reportsystem.restaurant.domain.port.outbound;

import com.reportsystem.restaurant.domain.model.Outlet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OutletRepository {
    Outlet save(Outlet outlet);
    Optional<Outlet> findById(UUID id);
    List<Outlet> findByTenantId(UUID tenantId);
    List<Outlet> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

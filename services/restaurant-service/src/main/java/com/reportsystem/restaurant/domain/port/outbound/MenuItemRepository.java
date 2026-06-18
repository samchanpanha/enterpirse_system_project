package com.reportsystem.restaurant.domain.port.outbound;

import com.reportsystem.restaurant.domain.model.MenuItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuItemRepository {
    MenuItem save(MenuItem item);
    Optional<MenuItem> findById(UUID id);
    List<MenuItem> findByCategoryId(UUID categoryId);
    List<MenuItem> findActiveByTenantId(UUID tenantId);
    List<MenuItem> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

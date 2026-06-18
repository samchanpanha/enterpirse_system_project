package com.reportsystem.restaurant.domain.port.outbound;

import com.reportsystem.restaurant.domain.model.Category;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository {
    Category save(Category category);
    Optional<Category> findById(UUID id);
    List<Category> findByTenantIdAndOutletId(UUID tenantId, UUID outletId);
    List<Category> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

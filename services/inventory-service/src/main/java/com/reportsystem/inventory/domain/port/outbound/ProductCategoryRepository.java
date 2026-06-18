package com.reportsystem.inventory.domain.port.outbound;

import com.reportsystem.inventory.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductCategoryRepository {
    ProductCategory save(ProductCategory c);
    Optional<ProductCategory> findById(UUID id);
    List<ProductCategory> findByTenantId(UUID tenantId);
}

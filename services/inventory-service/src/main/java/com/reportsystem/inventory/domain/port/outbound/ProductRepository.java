package com.reportsystem.inventory.domain.port.outbound;

import com.reportsystem.inventory.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository {
    Product save(Product p);
    Optional<Product> findById(UUID id);
    List<Product> findByTenantId(UUID tenantId);
    List<Product> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    List<Product> findByCategoryId(UUID categoryId);
    Optional<Product> findByBarcode(String barcode);
}

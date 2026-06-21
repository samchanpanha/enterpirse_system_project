package com.reportsystem.inventory.domain.port.inbound;

import com.reportsystem.inventory.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductService {
    Product createProduct(UUID tenantId, UUID branchId, UUID categoryId, String name, String sku, java.math.BigDecimal unitPrice, java.math.BigDecimal costPrice);
    Optional<Product> getProductById(UUID id);
    List<Product> getProductsByTenant(UUID tenantId);
    List<Product> getProductsByTenantAndBranch(UUID tenantId, UUID branchId);
    Product updateProduct(UUID id, String name, java.math.BigDecimal price, boolean active);
    Optional<Product> getProductByBarcode(String barcode);
}

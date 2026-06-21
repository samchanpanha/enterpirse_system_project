package com.reportsystem.inventory.infrastructure.persistence.repository;

import com.reportsystem.inventory.infrastructure.persistence.entity.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProductRepository extends JpaRepository<ProductEntity, UUID> {
    List<ProductEntity> findByTenantId(UUID tenantId);
    List<ProductEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    List<ProductEntity> findByCategoryId(UUID categoryId);
    Optional<ProductEntity> findByBarcode(String barcode);
}

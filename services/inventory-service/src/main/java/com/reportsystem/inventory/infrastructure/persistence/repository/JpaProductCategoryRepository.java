package com.reportsystem.inventory.infrastructure.persistence.repository;

import com.reportsystem.inventory.infrastructure.persistence.entity.*;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaProductCategoryRepository extends JpaRepository<ProductCategoryEntity, UUID> {
    List<ProductCategoryEntity> findByTenantId(UUID tenantId);
}

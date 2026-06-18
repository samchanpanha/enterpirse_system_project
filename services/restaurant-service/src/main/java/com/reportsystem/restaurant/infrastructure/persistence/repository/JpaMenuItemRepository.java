package com.reportsystem.restaurant.infrastructure.persistence.repository;

import com.reportsystem.restaurant.infrastructure.persistence.entity.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaMenuItemRepository extends JpaRepository<MenuItemEntity, UUID> {
    List<MenuItemEntity> findByCategoryId(UUID categoryId);
    List<MenuItemEntity> findByTenantIdAndActiveTrue(UUID tenantId);
    List<MenuItemEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

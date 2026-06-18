package com.reportsystem.restaurant.infrastructure.persistence.repository;

import com.reportsystem.restaurant.infrastructure.persistence.entity.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCategoryRepository extends JpaRepository<CategoryEntity, UUID> {
    List<CategoryEntity> findByTenantIdAndOutletId(UUID tenantId, UUID outletId);
    List<CategoryEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

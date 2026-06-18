package com.reportsystem.restaurant.infrastructure.persistence.repository;

import com.reportsystem.restaurant.infrastructure.persistence.entity.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaOutletRepository extends JpaRepository<OutletEntity, UUID> {
    List<OutletEntity> findByTenantId(UUID tenantId);
    List<OutletEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

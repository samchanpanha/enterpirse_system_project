package com.reportsystem.inventory.infrastructure.persistence.repository;

import com.reportsystem.inventory.infrastructure.persistence.entity.*;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaSupplierRepository extends JpaRepository<SupplierEntity, UUID> {
    List<SupplierEntity> findByTenantId(UUID tenantId);
    List<SupplierEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

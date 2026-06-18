package com.reportsystem.inventory.infrastructure.persistence.repository;

import com.reportsystem.inventory.infrastructure.persistence.entity.*;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPurchaseOrderRepository extends JpaRepository<PurchaseOrderEntity, UUID> {
    List<PurchaseOrderEntity> findByTenantId(UUID tenantId);
    List<PurchaseOrderEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    List<PurchaseOrderEntity> findBySupplierId(UUID supplierId);
}

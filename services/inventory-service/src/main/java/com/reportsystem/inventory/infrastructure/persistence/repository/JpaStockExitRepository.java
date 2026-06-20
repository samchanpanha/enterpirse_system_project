package com.reportsystem.inventory.infrastructure.persistence.repository;

import com.reportsystem.inventory.infrastructure.persistence.entity.*;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaStockExitRepository extends JpaRepository<StockExitEntity, UUID> {
    List<StockExitEntity> findByProductIdAndWarehouseId(UUID productId, UUID warehouseId);
    List<StockExitEntity> findByTenantIdAndBranchIdAndProductId(UUID tenantId, UUID branchId, UUID productId);
}

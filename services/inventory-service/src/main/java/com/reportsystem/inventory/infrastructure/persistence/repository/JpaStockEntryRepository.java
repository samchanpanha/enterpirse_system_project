package com.reportsystem.inventory.infrastructure.persistence.repository;

import com.reportsystem.inventory.infrastructure.persistence.entity.*;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaStockEntryRepository extends JpaRepository<StockEntryEntity, UUID> {
    List<StockEntryEntity> findByProductIdAndWarehouseId(UUID productId, UUID warehouseId);
    List<StockEntryEntity> findByTenantIdAndBranchIdAndProductId(UUID tenantId, UUID branchId, UUID productId);
}

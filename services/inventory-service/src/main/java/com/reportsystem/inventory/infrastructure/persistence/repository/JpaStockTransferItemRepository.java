package com.reportsystem.inventory.infrastructure.persistence.repository;

import com.reportsystem.inventory.infrastructure.persistence.entity.StockTransferItemEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaStockTransferItemRepository extends JpaRepository<StockTransferItemEntity, UUID> {
    List<StockTransferItemEntity> findByTransferIdOrderByLineOrder(UUID transferId);
    List<StockTransferItemEntity> findByTransferIdIn(java.util.Collection<UUID> transferIds);
}

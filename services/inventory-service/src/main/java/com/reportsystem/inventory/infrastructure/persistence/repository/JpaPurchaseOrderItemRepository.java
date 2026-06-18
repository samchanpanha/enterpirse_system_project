package com.reportsystem.inventory.infrastructure.persistence.repository;

import com.reportsystem.inventory.infrastructure.persistence.entity.*;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItemEntity, UUID> {
    List<PurchaseOrderItemEntity> findByPurchaseOrderId(UUID poId);
}

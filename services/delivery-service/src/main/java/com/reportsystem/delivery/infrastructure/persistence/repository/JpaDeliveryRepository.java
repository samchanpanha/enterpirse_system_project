package com.reportsystem.delivery.infrastructure.persistence.repository;

import com.reportsystem.delivery.infrastructure.persistence.entity.*;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaDeliveryRepository extends JpaRepository<DeliveryEntity, UUID> {
    List<DeliveryEntity> findByTenantId(UUID tenantId);
    List<DeliveryEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

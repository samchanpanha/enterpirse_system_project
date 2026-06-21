package com.reportsystem.delivery.infrastructure.persistence.repository;

import com.reportsystem.delivery.infrastructure.persistence.entity.*;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaDeliveryZoneRepository extends JpaRepository<DeliveryZoneEntity, UUID> {
    List<DeliveryZoneEntity> findByTenantId(UUID tenantId);
    List<DeliveryZoneEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

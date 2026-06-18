package com.reportsystem.inventory.infrastructure.persistence.repository;

import com.reportsystem.inventory.infrastructure.persistence.entity.*;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaWarehouseRepository extends JpaRepository<WarehouseEntity, UUID> {
    List<WarehouseEntity> findByTenantId(UUID tenantId);
}

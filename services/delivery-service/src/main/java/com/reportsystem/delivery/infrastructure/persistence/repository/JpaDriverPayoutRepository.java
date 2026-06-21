package com.reportsystem.delivery.infrastructure.persistence.repository;

import com.reportsystem.delivery.infrastructure.persistence.entity.*;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaDriverPayoutRepository extends JpaRepository<DriverPayoutEntity, UUID> {
    List<DriverPayoutEntity> findByTenantId(UUID tenantId);
    List<DriverPayoutEntity> findByDriverId(UUID driverId);
}

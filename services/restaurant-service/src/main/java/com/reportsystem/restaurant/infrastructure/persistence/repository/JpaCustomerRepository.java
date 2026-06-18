package com.reportsystem.restaurant.infrastructure.persistence.repository;

import com.reportsystem.restaurant.infrastructure.persistence.entity.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCustomerRepository extends JpaRepository<CustomerEntity, UUID> {
    Optional<CustomerEntity> findByPhoneAndTenantId(String phone, UUID tenantId);
    List<CustomerEntity> findByOutletId(UUID outletId);
    List<CustomerEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

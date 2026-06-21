package com.reportsystem.realty.infrastructure.persistence.repository;

import com.reportsystem.realty.infrastructure.persistence.entity.CustomerEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaCustomerRepository extends JpaRepository<CustomerEntity, UUID> {
    List<CustomerEntity> findByTenantId(UUID tenantId);
    List<CustomerEntity> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

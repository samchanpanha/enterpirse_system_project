package com.reportsystem.realty.domain.port.outbound;

import com.reportsystem.realty.domain.model.Customer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findById(UUID id);
    List<Customer> findByTenantId(UUID tenantId);
    List<Customer> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

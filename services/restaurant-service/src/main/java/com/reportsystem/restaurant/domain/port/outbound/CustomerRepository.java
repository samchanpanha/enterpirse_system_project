package com.reportsystem.restaurant.domain.port.outbound;

import com.reportsystem.restaurant.domain.model.Customer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerRepository {
    Customer save(Customer customer);
    Optional<Customer> findById(UUID id);
    Optional<Customer> findByPhoneAndTenantId(String phone, UUID tenantId);
    List<Customer> findByOutletId(UUID outletId);
    List<Customer> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

package com.reportsystem.realty.domain.port.inbound;

import com.reportsystem.realty.domain.model.Customer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerService {
    Customer createCustomer(UUID tenantId, UUID branchId, String name, String type);
    Optional<Customer> getCustomerById(UUID id);
    List<Customer> getCustomersByTenant(UUID tenantId);
    List<Customer> getCustomersByTenantAndBranch(UUID tenantId, UUID branchId);
    Customer updateCustomer(Customer customer);
}

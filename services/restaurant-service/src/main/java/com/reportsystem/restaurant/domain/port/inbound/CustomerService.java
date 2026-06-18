package com.reportsystem.restaurant.domain.port.inbound;

import com.reportsystem.restaurant.domain.model.Customer;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerService {
    Customer createCustomer(UUID tenantId, UUID branchId, UUID outletId, String name, String phone);
    Optional<Customer> getCustomerById(UUID id);
    Optional<Customer> findCustomerByPhone(UUID tenantId, String phone);
    List<Customer> getCustomersByOutlet(UUID outletId);
    Customer updateVisit(UUID id);
}

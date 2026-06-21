package com.reportsystem.realty.domain.service;

import com.reportsystem.realty.domain.model.Customer;
import com.reportsystem.realty.domain.port.inbound.CustomerService;
import com.reportsystem.realty.domain.port.outbound.CustomerRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerServiceImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    public Customer createCustomer(UUID tenantId, UUID branchId, String name, String type) {
        Customer customer = Customer.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId)
                .name(name).type(type)
                .createdAt(Instant.now()).build();
        return customerRepository.save(customer);
    }

    @Override
    public Optional<Customer> getCustomerById(UUID id) {
        return customerRepository.findById(id);
    }

    @Override
    public List<Customer> getCustomersByTenant(UUID tenantId) {
        return customerRepository.findByTenantId(tenantId);
    }

    @Override
    public List<Customer> getCustomersByTenantAndBranch(UUID tenantId, UUID branchId) {
        return customerRepository.findByTenantIdAndBranchId(tenantId, branchId);
    }

    @Override
    public Customer updateCustomer(Customer customer) {
        return customerRepository.save(customer);
    }
}

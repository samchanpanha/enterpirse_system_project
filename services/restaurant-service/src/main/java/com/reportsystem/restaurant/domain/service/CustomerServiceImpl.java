package com.reportsystem.restaurant.domain.service;

import com.reportsystem.restaurant.domain.model.Customer;
import com.reportsystem.restaurant.domain.port.inbound.CustomerService;
import com.reportsystem.restaurant.domain.port.outbound.CustomerRepository;
import java.math.BigDecimal;
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
    public Customer createCustomer(UUID tenantId, UUID branchId, UUID outletId, String name, String phone) {
        Customer customer = Customer.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId).outletId(outletId)
                .name(name).phone(phone).vip(false).totalVisits(0).totalSpent(BigDecimal.ZERO)
                .createdAt(Instant.now()).build();
        return customerRepository.save(customer);
    }

    @Override
    public Optional<Customer> getCustomerById(UUID id) {
        return customerRepository.findById(id);
    }

    @Override
    public Optional<Customer> findCustomerByPhone(UUID tenantId, String phone) {
        return customerRepository.findByPhoneAndTenantId(phone, tenantId);
    }

    @Override
    public List<Customer> getCustomersByOutlet(UUID outletId) {
        return customerRepository.findByOutletId(outletId);
    }

    @Override
    public Customer updateVisit(UUID id) {
        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));
        Customer updated = Customer.builder()
                .id(existing.getId()).tenantId(existing.getTenantId()).outletId(existing.getOutletId())
                .name(existing.getName()).phone(existing.getPhone()).email(existing.getEmail())
                .birthday(existing.getBirthday()).vip(existing.isVip()).notes(existing.getNotes())
                .totalVisits(existing.getTotalVisits() + 1).totalSpent(existing.getTotalSpent())
                .lastVisitAt(Instant.now())
                .createdAt(existing.getCreatedAt()).updatedAt(Instant.now())
                .build();
        return customerRepository.save(updated);
    }

    @Override
    public Customer updateCustomer(UUID id, String name, String phone, String email, boolean vip) {
        Customer existing = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Customer not found: " + id));
        Customer updated = Customer.builder()
                .id(existing.getId()).tenantId(existing.getTenantId()).outletId(existing.getOutletId())
                .name(name != null ? name : existing.getName())
                .phone(phone != null ? phone : existing.getPhone())
                .email(email != null ? email : existing.getEmail())
                .birthday(existing.getBirthday()).vip(vip).notes(existing.getNotes())
                .totalVisits(existing.getTotalVisits()).totalSpent(existing.getTotalSpent())
                .lastVisitAt(existing.getLastVisitAt())
                .createdAt(existing.getCreatedAt()).updatedAt(Instant.now())
                .build();
        return customerRepository.save(updated);
    }
}

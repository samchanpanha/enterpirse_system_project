package com.reportsystem.restaurant.infrastructure.persistence.adapter;

import com.reportsystem.restaurant.domain.model.Customer;
import com.reportsystem.restaurant.domain.port.outbound.CustomerRepository;
import com.reportsystem.restaurant.infrastructure.persistence.entity.CustomerEntity;
import com.reportsystem.restaurant.infrastructure.persistence.repository.JpaCustomerRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaCustomerAdapter implements CustomerRepository {

    private final JpaCustomerRepository repo;
    public JpaCustomerAdapter(JpaCustomerRepository repo) { this.repo = repo; }

    @Override
    public Customer save(Customer customer) {
        CustomerEntity e = new CustomerEntity();
        e.setId(customer.getId());
        e.setTenantId(customer.getTenantId());
        e.setBranchId(customer.getBranchId());
        e.setOutletId(customer.getOutletId());
        e.setName(customer.getName());
        e.setPhone(customer.getPhone());
        e.setEmail(customer.getEmail());
        e.setBirthday(customer.getBirthday());
        e.setVip(customer.isVip());
        e.setNotes(customer.getNotes());
        e.setTotalVisits(customer.getTotalVisits());
        e.setTotalSpent(customer.getTotalSpent());
        e.setLastVisitAt(customer.getLastVisitAt());
        e.setCreatedAt(Instant.now());
        CustomerEntity saved = repo.save(e);
        return toDomain(saved);
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Customer> findByPhoneAndTenantId(String phone, UUID tenantId) {
        return repo.findByPhoneAndTenantId(phone, tenantId).map(this::toDomain);
    }

    @Override
    public List<Customer> findByOutletId(UUID outletId) {
        return repo.findByOutletId(outletId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Customer> findByTenantIdAndBranchId(UUID tenantId, UUID branchId) {
        return repo.findByTenantIdAndBranchId(tenantId, branchId).stream().map(this::toDomain).toList();
    }

    private Customer toDomain(CustomerEntity e) {
        return Customer.builder()
                .id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId()).outletId(e.getOutletId())
                .name(e.getName()).phone(e.getPhone()).email(e.getEmail())
                .birthday(e.getBirthday()).vip(e.isVip()).notes(e.getNotes())
                .totalVisits(e.getTotalVisits()).totalSpent(e.getTotalSpent())
                .lastVisitAt(e.getLastVisitAt())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }
}

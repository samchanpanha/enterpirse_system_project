package com.reportsystem.realty.infrastructure.persistence.adapter;

import com.reportsystem.realty.domain.model.Customer;
import com.reportsystem.realty.domain.port.outbound.CustomerRepository;
import com.reportsystem.realty.infrastructure.persistence.entity.CustomerEntity;
import com.reportsystem.realty.infrastructure.persistence.repository.JpaCustomerRepository;
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
        e.setName(customer.getName());
        e.setPhone(customer.getPhone());
        e.setEmail(customer.getEmail());
        e.setType(customer.getType() != null ? customer.getType() : "buyer");
        e.setNotes(customer.getNotes());
        e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e));
    }

    @Override
    public Optional<Customer> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Customer> findByTenantId(UUID tenantId) {
        return repo.findByTenantId(tenantId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Customer> findByTenantIdAndBranchId(UUID tenantId, UUID branchId) {
        return repo.findByTenantIdAndBranchId(tenantId, branchId).stream().map(this::toDomain).toList();
    }

    private Customer toDomain(CustomerEntity e) {
        return Customer.builder()
                .id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId())
                .name(e.getName()).phone(e.getPhone()).email(e.getEmail())
                .type(e.getType()).notes(e.getNotes())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }
}

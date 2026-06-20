package com.reportsystem.inventory.infrastructure.persistence.adapter;

import com.reportsystem.inventory.domain.model.*;
import com.reportsystem.inventory.domain.port.outbound.*;
import com.reportsystem.inventory.infrastructure.persistence.entity.*;
import com.reportsystem.inventory.infrastructure.persistence.repository.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class JpaSupplierAdapter implements SupplierRepository {
    private final JpaSupplierRepository repo;
    public JpaSupplierAdapter(JpaSupplierRepository repo) { this.repo = repo; }
    @Override public Supplier save(Supplier s) {
        SupplierEntity e = new SupplierEntity(); e.setId(s.getId()); e.setTenantId(s.getTenantId()); e.setBranchId(s.getBranchId());
        e.setName(s.getName()); e.setContactPerson(s.getContactPerson()); e.setPhone(s.getPhone());
        e.setEmail(s.getEmail()); e.setAddress(s.getAddress()); e.setTaxNumber(s.getTaxNumber());
        e.setPaymentTerms(s.getPaymentTerms()); e.setCurrency(s.getCurrency()); e.setActive(s.isActive());
        e.setNotes(s.getNotes()); e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e));
    }
    @Override public Optional<Supplier> findById(UUID id) { return repo.findById(id).map(this::toDomain); }
    @Override public List<Supplier> findByTenantId(UUID t) { return repo.findByTenantId(t).stream().map(this::toDomain).toList(); }
    @Override public List<Supplier> findByTenantIdAndBranchId(UUID t, UUID b) { return repo.findByTenantIdAndBranchId(t, b).stream().map(this::toDomain).toList(); }
    @Override public void deleteById(UUID id) { repo.deleteById(id); }
    private Supplier toDomain(SupplierEntity e) { return Supplier.builder().id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId()).name(e.getName()).contactPerson(e.getContactPerson()).phone(e.getPhone()).email(e.getEmail()).address(e.getAddress()).taxNumber(e.getTaxNumber()).paymentTerms(e.getPaymentTerms()).currency(e.getCurrency()).active(e.isActive()).notes(e.getNotes()).createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).build(); }
}
package com.reportsystem.inventory.domain.service;

import com.reportsystem.inventory.domain.model.*;
import com.reportsystem.inventory.domain.port.inbound.*;
import com.reportsystem.inventory.domain.port.outbound.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class SupplierServiceImpl implements SupplierService {
    private final SupplierRepository supplierRepo;
    public SupplierServiceImpl(SupplierRepository supplierRepo) { this.supplierRepo = supplierRepo; }
    @Override public Supplier createSupplier(UUID tenantId, UUID branchId, String name, String phone) {
        return supplierRepo.save(Supplier.builder().id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId).name(name).phone(phone).active(true).createdAt(Instant.now()).build());
    }
    @Override public Optional<Supplier> getSupplierById(UUID id) { return supplierRepo.findById(id); }
    @Override public List<Supplier> getSuppliersByTenant(UUID tenantId) { return supplierRepo.findByTenantId(tenantId); }
    @Override public List<Supplier> getSuppliersByTenantAndBranch(UUID tenantId, UUID branchId) { return supplierRepo.findByTenantIdAndBranchId(tenantId, branchId); }
    @Override public Supplier updateSupplier(UUID id, String name, String phone, boolean active) {
        Supplier existing = supplierRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Supplier not found: " + id));
        Supplier updated = Supplier.builder().id(existing.getId()).tenantId(existing.getTenantId()).branchId(existing.getBranchId())
                .name(name != null ? name : existing.getName()).contactPerson(existing.getContactPerson())
                .phone(phone != null ? phone : existing.getPhone()).email(existing.getEmail()).address(existing.getAddress())
                .taxNumber(existing.getTaxNumber()).paymentTerms(existing.getPaymentTerms()).currency(existing.getCurrency())
                .active(active).notes(existing.getNotes()).createdAt(existing.getCreatedAt()).updatedAt(Instant.now()).build();
        return supplierRepo.save(updated);
    }
    @Override public void deleteSupplier(UUID id) { supplierRepo.deleteById(id); }
}

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
}
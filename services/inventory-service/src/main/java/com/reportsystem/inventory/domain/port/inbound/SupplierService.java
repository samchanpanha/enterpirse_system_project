package com.reportsystem.inventory.domain.port.inbound;

import com.reportsystem.inventory.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupplierService {
    Supplier createSupplier(UUID tenantId, UUID branchId, String name, String phone);
    Optional<Supplier> getSupplierById(UUID id);
    List<Supplier> getSuppliersByTenant(UUID tenantId);
    List<Supplier> getSuppliersByTenantAndBranch(UUID tenantId, UUID branchId);
}

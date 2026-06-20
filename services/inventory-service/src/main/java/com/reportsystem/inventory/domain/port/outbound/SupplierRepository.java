package com.reportsystem.inventory.domain.port.outbound;

import com.reportsystem.inventory.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SupplierRepository {
    Supplier save(Supplier s);
    Optional<Supplier> findById(UUID id);
    List<Supplier> findByTenantId(UUID tenantId);
    List<Supplier> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    void deleteById(UUID id);
}

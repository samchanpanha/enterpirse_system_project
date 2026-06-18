package com.reportsystem.inventory.domain.port.outbound;

import com.reportsystem.inventory.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseOrderRepository {
    PurchaseOrder save(PurchaseOrder po);
    Optional<PurchaseOrder> findById(UUID id);
    List<PurchaseOrder> findByTenantId(UUID tenantId);
    List<PurchaseOrder> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    List<PurchaseOrder> findBySupplierId(UUID supplierId);
    String generatePoNumber(UUID tenantId);
}

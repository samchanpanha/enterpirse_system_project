package com.reportsystem.inventory.domain.port.inbound;

import com.reportsystem.inventory.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseOrderService {
    PurchaseOrder createPO(UUID tenantId, UUID branchId, UUID supplierId, java.time.LocalDate orderDate);
    Optional<PurchaseOrder> getPOById(UUID id);
    List<PurchaseOrder> getPOsByTenant(UUID tenantId);
    List<PurchaseOrder> getPOsByTenantAndBranch(UUID tenantId, UUID branchId);
    PurchaseOrder addItem(UUID poId, UUID productId, java.math.BigDecimal qty, java.math.BigDecimal unitCost);
    PurchaseOrder receiveItems(UUID poId);
    PurchaseOrder cancelPO(UUID id);
}

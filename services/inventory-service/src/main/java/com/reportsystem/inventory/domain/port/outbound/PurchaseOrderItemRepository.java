package com.reportsystem.inventory.domain.port.outbound;

import com.reportsystem.inventory.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurchaseOrderItemRepository {
    PurchaseOrderItem save(PurchaseOrderItem item);
    List<PurchaseOrderItem> findByPurchaseOrderId(UUID poId);
}

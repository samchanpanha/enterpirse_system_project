package com.reportsystem.restaurant.domain.port.inbound;

import com.reportsystem.restaurant.domain.model.Order;

public interface PosService {
    Order createPosOrder(java.util.UUID tenantId, java.util.UUID branchId, java.util.UUID outletId, java.util.UUID tableId, java.util.List<PosOrderItem> items);
    Order completePosOrder(java.util.UUID orderId, java.math.BigDecimal amountTendered, String paymentMethod);
    record PosOrderItem(java.util.UUID menuItemId, int quantity, String modifiers) {}
}

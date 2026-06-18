package com.reportsystem.restaurant.domain.service;

import com.reportsystem.restaurant.domain.model.*;
import com.reportsystem.restaurant.domain.port.inbound.PosService;
import com.reportsystem.restaurant.domain.port.inbound.OrderService;
import java.math.BigDecimal;
import java.util.UUID;

public class PosServiceImpl implements PosService {

    private final OrderService orderService;

    public PosServiceImpl(OrderService orderService) {
        this.orderService = orderService;
    }

    @Override
    public Order createPosOrder(UUID tenantId, UUID branchId, UUID outletId, UUID tableId, java.util.List<PosOrderItem> items) {
        Order order = orderService.createOrder(tenantId, branchId, outletId, tableId, null, "dine_in", null, null);
        for (PosOrderItem item : items) {
            order = orderService.addItem(order.getId(), item.menuItemId(), item.quantity(), BigDecimal.ZERO, item.modifiers());
        }
        return order;
    }

    @Override
    public Order completePosOrder(UUID orderId, BigDecimal amountTendered, String paymentMethod) {
        return orderService.completeOrder(orderId, BigDecimal.ZERO, BigDecimal.ZERO, paymentMethod);
    }
}

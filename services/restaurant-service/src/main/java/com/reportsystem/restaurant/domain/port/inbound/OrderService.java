package com.reportsystem.restaurant.domain.port.inbound;

import com.reportsystem.restaurant.domain.model.Order;
import com.reportsystem.restaurant.domain.model.OrderItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderService {
    Order createOrder(UUID tenantId, UUID branchId, UUID outletId, UUID tableId, UUID customerId, String type, String notes, UUID servedBy);
    Order addItem(UUID orderId, UUID menuItemId, int quantity, java.math.BigDecimal unitPrice, String modifiers);
    Order removeItem(UUID orderId, UUID orderItemId);
    Optional<Order> getOrderById(UUID id);
    List<Order> getOrdersByOutlet(UUID outletId, String status);
    Order updateStatus(UUID id, String status);
    Order completeOrder(UUID id, java.math.BigDecimal discount, java.math.BigDecimal serviceCharge, String paymentMethod);
    List<OrderItem> getOrderItems(UUID orderId);
}

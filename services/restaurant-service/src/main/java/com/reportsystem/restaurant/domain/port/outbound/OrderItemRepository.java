package com.reportsystem.restaurant.domain.port.outbound;

import com.reportsystem.restaurant.domain.model.OrderItem;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderItemRepository {
    OrderItem save(OrderItem item);
    Optional<OrderItem> findById(UUID id);
    List<OrderItem> findByOrderId(UUID orderId);
    void deleteById(UUID id);
}

package com.reportsystem.restaurant.domain.port.outbound;

import com.reportsystem.restaurant.domain.model.Order;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface OrderRepository {
    Order save(Order order);
    Optional<Order> findById(UUID id);
    List<Order> findByOutletIdAndStatus(UUID outletId, String status);
    List<Order> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    String generateOrderNumber(UUID outletId);
}

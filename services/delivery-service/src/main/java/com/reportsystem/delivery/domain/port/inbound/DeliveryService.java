package com.reportsystem.delivery.domain.port.inbound;

import com.reportsystem.delivery.domain.model.Delivery;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DeliveryService {
    Delivery createDelivery(UUID tenantId, UUID branchId, UUID outletId, UUID orderId, UUID driverId,
                            String customerName, String customerPhone, String deliveryAddress, String pickupAddress);
    Optional<Delivery> getDeliveryById(UUID id);
    List<Delivery> getDeliveriesByTenant(UUID tenantId);
    List<Delivery> getDeliveriesByTenantAndBranch(UUID tenantId, UUID branchId);
    Delivery updateStatus(UUID id, String status);
}

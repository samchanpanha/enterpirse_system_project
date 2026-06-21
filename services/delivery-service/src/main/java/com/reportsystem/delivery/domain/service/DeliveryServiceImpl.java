package com.reportsystem.delivery.domain.service;

import com.reportsystem.delivery.domain.model.Delivery;
import com.reportsystem.delivery.domain.port.inbound.DeliveryService;
import com.reportsystem.delivery.domain.port.outbound.DeliveryRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;

    public DeliveryServiceImpl(DeliveryRepository deliveryRepository) {
        this.deliveryRepository = deliveryRepository;
    }

    @Override
    public Delivery createDelivery(UUID tenantId, UUID branchId, UUID outletId, UUID orderId, UUID driverId,
                                   String customerName, String customerPhone, String deliveryAddress, String pickupAddress) {
        Delivery delivery = Delivery.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId).outletId(outletId)
                .orderId(orderId).driverId(driverId)
                .customerName(customerName).customerPhone(customerPhone)
                .deliveryAddress(deliveryAddress).pickupAddress(pickupAddress)
                .status("pending").deliveryFee(java.math.BigDecimal.ZERO)
                .metadata("{}").createdAt(Instant.now()).build();
        return deliveryRepository.save(delivery);
    }

    @Override
    public Optional<Delivery> getDeliveryById(UUID id) {
        return deliveryRepository.findById(id);
    }

    @Override
    public List<Delivery> getDeliveriesByTenant(UUID tenantId) {
        return deliveryRepository.findByTenantId(tenantId);
    }

    @Override
    public List<Delivery> getDeliveriesByTenantAndBranch(UUID tenantId, UUID branchId) {
        return deliveryRepository.findByTenantIdAndBranchId(tenantId, branchId);
    }

    @Override
    public Delivery updateStatus(UUID id, String status) {
        Delivery existing = deliveryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found: " + id));
        Delivery updated = Delivery.builder()
                .id(existing.getId()).tenantId(existing.getTenantId()).branchId(existing.getBranchId())
                .outletId(existing.getOutletId()).orderId(existing.getOrderId()).driverId(existing.getDriverId())
                .customerName(existing.getCustomerName()).customerPhone(existing.getCustomerPhone())
                .deliveryAddress(existing.getDeliveryAddress()).pickupAddress(existing.getPickupAddress())
                .status(status).scheduledAt(existing.getScheduledAt())
                .pickedUpAt("picked_up".equals(status) ? Instant.now() : existing.getPickedUpAt())
                .deliveredAt("delivered".equals(status) ? Instant.now() : existing.getDeliveredAt())
                .deliveryFee(existing.getDeliveryFee()).distanceKm(existing.getDistanceKm())
                .notes(existing.getNotes()).metadata(existing.getMetadata())
                .createdAt(existing.getCreatedAt()).updatedAt(Instant.now())
                .build();
        return deliveryRepository.save(updated);
    }
}

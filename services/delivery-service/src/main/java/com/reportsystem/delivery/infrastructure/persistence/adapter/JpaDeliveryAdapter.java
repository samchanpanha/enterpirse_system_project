package com.reportsystem.delivery.infrastructure.persistence.adapter;

import com.reportsystem.delivery.domain.model.Delivery;
import com.reportsystem.delivery.domain.port.outbound.DeliveryRepository;
import com.reportsystem.delivery.infrastructure.persistence.entity.DeliveryEntity;
import com.reportsystem.delivery.infrastructure.persistence.repository.JpaDeliveryRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaDeliveryAdapter implements DeliveryRepository {

    private final JpaDeliveryRepository repo;
    public JpaDeliveryAdapter(JpaDeliveryRepository repo) { this.repo = repo; }

    @Override
    public Delivery save(Delivery delivery) {
        DeliveryEntity e = toEntity(delivery);
        DeliveryEntity saved = repo.save(e);
        return toDomain(saved);
    }

    @Override
    public Optional<Delivery> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Delivery> findByTenantId(UUID tenantId) {
        return repo.findByTenantId(tenantId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Delivery> findByTenantIdAndBranchId(UUID tenantId, UUID branchId) {
        return repo.findByTenantIdAndBranchId(tenantId, branchId).stream().map(this::toDomain).toList();
    }

    private DeliveryEntity toEntity(Delivery d) {
        DeliveryEntity e = new DeliveryEntity();
        e.setId(d.getId());
        e.setTenantId(d.getTenantId());
        e.setBranchId(d.getBranchId());
        e.setOutletId(d.getOutletId());
        e.setOrderId(d.getOrderId());
        e.setDriverId(d.getDriverId());
        e.setCustomerName(d.getCustomerName());
        e.setCustomerPhone(d.getCustomerPhone());
        e.setDeliveryAddress(d.getDeliveryAddress());
        e.setPickupAddress(d.getPickupAddress());
        e.setStatus(d.getStatus());
        e.setScheduledAt(d.getScheduledAt());
        e.setPickedUpAt(d.getPickedUpAt());
        e.setDeliveredAt(d.getDeliveredAt());
        e.setDeliveryFee(d.getDeliveryFee());
        e.setDistanceKm(d.getDistanceKm());
        e.setNotes(d.getNotes());
        e.setMetadata(d.getMetadata() != null ? d.getMetadata() : "{}");
        e.setCreatedAt(d.getCreatedAt());
        e.setUpdatedAt(Instant.now());
        return e;
    }

    private Delivery toDomain(DeliveryEntity e) {
        return Delivery.builder()
                .id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId())
                .outletId(e.getOutletId()).orderId(e.getOrderId()).driverId(e.getDriverId())
                .customerName(e.getCustomerName()).customerPhone(e.getCustomerPhone())
                .deliveryAddress(e.getDeliveryAddress()).pickupAddress(e.getPickupAddress())
                .status(e.getStatus()).scheduledAt(e.getScheduledAt())
                .pickedUpAt(e.getPickedUpAt()).deliveredAt(e.getDeliveredAt())
                .deliveryFee(e.getDeliveryFee()).distanceKm(e.getDistanceKm())
                .notes(e.getNotes()).metadata(e.getMetadata())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }
}

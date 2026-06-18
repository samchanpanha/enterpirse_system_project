package com.reportsystem.restaurant.infrastructure.persistence.adapter;

import com.reportsystem.restaurant.domain.model.OrderItem;
import com.reportsystem.restaurant.domain.port.outbound.OrderItemRepository;
import com.reportsystem.restaurant.infrastructure.persistence.entity.OrderItemEntity;
import com.reportsystem.restaurant.infrastructure.persistence.repository.JpaOrderItemRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaOrderItemAdapter implements OrderItemRepository {

    private final JpaOrderItemRepository repo;
    public JpaOrderItemAdapter(JpaOrderItemRepository repo) { this.repo = repo; }

    @Override
    public OrderItem save(OrderItem item) {
        OrderItemEntity e = new OrderItemEntity();
        e.setId(item.getId());
        e.setOrderId(item.getOrderId());
        e.setMenuItemId(item.getMenuItemId());
        e.setQuantity(item.getQuantity());
        e.setUnitPrice(item.getUnitPrice());
        e.setModifiers(item.getModifiers() != null ? item.getModifiers() : "[]");
        e.setSubtotal(item.getSubtotal());
        e.setStatus(item.getStatus() != null ? item.getStatus() : "pending");
        e.setNotes(item.getNotes());
        e.setCreatedAt(Instant.now());
        OrderItemEntity saved = repo.save(e);
        return toDomain(saved);
    }

    @Override
    public Optional<OrderItem> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<OrderItem> findByOrderId(UUID orderId) {
        return repo.findByOrderId(orderId).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        repo.deleteById(id);
    }

    private OrderItem toDomain(OrderItemEntity e) {
        return OrderItem.builder()
                .id(e.getId()).orderId(e.getOrderId()).menuItemId(e.getMenuItemId())
                .quantity(e.getQuantity()).unitPrice(e.getUnitPrice())
                .modifiers(e.getModifiers()).subtotal(e.getSubtotal())
                .status(e.getStatus()).notes(e.getNotes())
                .createdAt(e.getCreatedAt())
                .build();
    }
}

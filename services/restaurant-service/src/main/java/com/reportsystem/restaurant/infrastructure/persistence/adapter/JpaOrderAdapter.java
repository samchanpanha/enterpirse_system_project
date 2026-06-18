package com.reportsystem.restaurant.infrastructure.persistence.adapter;

import com.reportsystem.restaurant.domain.model.Order;
import com.reportsystem.restaurant.domain.port.outbound.OrderRepository;
import com.reportsystem.restaurant.infrastructure.persistence.entity.OrderEntity;
import com.reportsystem.restaurant.infrastructure.persistence.repository.JpaOrderRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaOrderAdapter implements OrderRepository {

    private final JpaOrderRepository repo;
    public JpaOrderAdapter(JpaOrderRepository repo) { this.repo = repo; }

    @Override
    public Order save(Order order) {
        OrderEntity e = new OrderEntity();
        e.setId(order.getId());
        e.setTenantId(order.getTenantId());
        e.setBranchId(order.getBranchId());
        e.setOutletId(order.getOutletId());
        e.setTableId(order.getTableId());
        e.setCustomerId(order.getCustomerId());
        e.setOrderNumber(order.getOrderNumber());
        e.setType(order.getType());
        e.setStatus(order.getStatus());
        e.setSubtotal(order.getSubtotal());
        e.setDiscount(order.getDiscount());
        e.setTaxAmount(order.getTaxAmount());
        e.setServiceCharge(order.getServiceCharge());
        e.setTotal(order.getTotal());
        e.setPaymentStatus(order.getPaymentStatus());
        e.setNotes(order.getNotes());
        e.setServedBy(order.getServedBy());
        e.setCreatedAt(order.getCreatedAt());
        e.setCompletedAt(order.getCompletedAt());
        OrderEntity saved = repo.save(e);
        return toDomain(saved);
    }

    @Override
    public Optional<Order> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Order> findByOutletIdAndStatus(UUID outletId, String status) {
        return repo.findByOutletIdAndStatus(outletId, status).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Order> findByTenantIdAndBranchId(UUID tenantId, UUID branchId) {
        return repo.findByTenantIdAndBranchId(tenantId, branchId).stream().map(this::toDomain).toList();
    }

    @Override
    public String generateOrderNumber(UUID outletId) {
        return "ORD-" + outletId.toString().substring(0, 8).toUpperCase() + "-" + System.currentTimeMillis();
    }

    private Order toDomain(OrderEntity e) {
        return Order.builder()
                .id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId()).outletId(e.getOutletId())
                .tableId(e.getTableId()).customerId(e.getCustomerId())
                .orderNumber(e.getOrderNumber()).type(e.getType()).status(e.getStatus())
                .subtotal(e.getSubtotal()).discount(e.getDiscount()).taxAmount(e.getTaxAmount())
                .serviceCharge(e.getServiceCharge()).total(e.getTotal())
                .paymentStatus(e.getPaymentStatus()).notes(e.getNotes()).servedBy(e.getServedBy())
                .createdAt(e.getCreatedAt()).completedAt(e.getCompletedAt())
                .build();
    }
}

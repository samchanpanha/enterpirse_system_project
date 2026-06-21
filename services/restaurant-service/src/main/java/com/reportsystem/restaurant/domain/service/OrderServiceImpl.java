package com.reportsystem.restaurant.domain.service;

import com.reportsystem.restaurant.domain.model.Order;
import com.reportsystem.restaurant.domain.model.OrderItem;
import com.reportsystem.restaurant.domain.port.inbound.OrderService;
import com.reportsystem.restaurant.domain.port.outbound.OrderItemRepository;
import com.reportsystem.restaurant.domain.port.outbound.OrderRepository;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public OrderServiceImpl(OrderRepository orderRepository, OrderItemRepository orderItemRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
    }

    @Override
    public Order createOrder(UUID tenantId, UUID branchId, UUID outletId, UUID tableId, UUID customerId, String type, String notes, UUID servedBy) {
        String orderNumber = orderRepository.generateOrderNumber(outletId);
        Order order = Order.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId).outletId(outletId)
                .tableId(tableId).customerId(customerId)
                .orderNumber(orderNumber).type(type != null ? type : "dine_in")
                .status("pending").subtotal(BigDecimal.ZERO).discount(BigDecimal.ZERO)
                .taxAmount(BigDecimal.ZERO).serviceCharge(BigDecimal.ZERO).total(BigDecimal.ZERO)
                .paymentStatus("unpaid").paymentMethod(null).notes(notes).servedBy(servedBy)
                .createdAt(Instant.now()).build();
        return orderRepository.save(order);
    }

    @Override
    public Order createOrderWithItems(UUID tenantId, UUID branchId, UUID outletId, UUID tableId, UUID customerId, String type, String notes, UUID servedBy, List<OrderItem> items) {
        Order order = createOrder(tenantId, branchId, outletId, tableId, customerId, type, notes, servedBy);
        if (items != null) {
            for (OrderItem item : items) {
                if (item == null || item.getMenuItemId() == null || item.getQuantity() <= 0) {
                    continue;
                }
                BigDecimal price = item.getUnitPrice() != null ? item.getUnitPrice() : BigDecimal.ZERO;
                addItem(order.getId(), item.getMenuItemId(), item.getQuantity(), price, item.getModifiers());
            }
        }
        return getOrderById(order.getId()).orElse(order);
    }

    @Override
    public Order addItem(UUID orderId, UUID menuItemId, int quantity, BigDecimal unitPrice, String modifiers) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
        BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(quantity));
        OrderItem item = OrderItem.builder()
                .id(UUID.randomUUID()).orderId(orderId).menuItemId(menuItemId)
                .quantity(quantity).unitPrice(unitPrice)
                .modifiers(modifiers != null ? modifiers : "[]")
                .subtotal(subtotal).status("pending")
                .createdAt(Instant.now()).build();
        orderItemRepository.save(item);
        return recalculateOrder(order);
    }

    @Override
    public Order removeItem(UUID orderId, UUID orderItemId) {
        orderItemRepository.deleteById(orderItemId);
        return recalculateOrder(orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId)));
    }

    @Override
    public Optional<Order> getOrderById(UUID id) {
        return orderRepository.findById(id);
    }

    @Override
    public List<Order> getOrdersByOutlet(UUID outletId, String status) {
        return orderRepository.findByOutletIdAndStatus(outletId, status);
    }

    @Override
    public List<Order> getOrdersByTenantAndBranch(UUID tenantId, UUID branchId) {
        return orderRepository.findByTenantIdAndBranchId(tenantId, branchId);
    }

    @Override
    public Order updateStatus(UUID id, String status) {
        Order existing = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
        Order updated = Order.builder()
                .id(existing.getId()).tenantId(existing.getTenantId()).outletId(existing.getOutletId())
                .tableId(existing.getTableId()).customerId(existing.getCustomerId())
                .orderNumber(existing.getOrderNumber()).type(existing.getType()).status(status)
                .subtotal(existing.getSubtotal()).discount(existing.getDiscount())
                .taxAmount(existing.getTaxAmount()).serviceCharge(existing.getServiceCharge())
                .total(existing.getTotal()).paymentStatus(existing.getPaymentStatus())
                .paymentMethod(existing.getPaymentMethod()).discountType(existing.getDiscountType())
                .notes(existing.getNotes()).servedBy(existing.getServedBy())
                .createdAt(existing.getCreatedAt()).completedAt(existing.getCompletedAt())
                .build();
        return orderRepository.save(updated);
    }

    @Override
    public Order updateOrderDiscount(UUID id, BigDecimal discount, String discountType) {
        Order existing = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
        Order updated = Order.builder()
                .id(existing.getId()).tenantId(existing.getTenantId()).outletId(existing.getOutletId())
                .tableId(existing.getTableId()).customerId(existing.getCustomerId())
                .orderNumber(existing.getOrderNumber()).type(existing.getType()).status(existing.getStatus())
                .subtotal(existing.getSubtotal())
                .discount(discount != null ? discount : BigDecimal.ZERO)
                .discountType(discountType)
                .taxAmount(existing.getTaxAmount()).serviceCharge(existing.getServiceCharge())
                .total(existing.getTotal()).paymentStatus(existing.getPaymentStatus())
                .paymentMethod(existing.getPaymentMethod())
                .notes(existing.getNotes()).servedBy(existing.getServedBy())
                .createdAt(existing.getCreatedAt()).completedAt(existing.getCompletedAt())
                .build();
        return orderRepository.save(updated);
    }

    @Override
    public Order updateOrderItemStatus(UUID orderItemId, String status) {
        orderItemRepository.updateStatus(orderItemId, status);
        return null;
    }

    @Override
    public Order completeOrder(UUID id, BigDecimal discount, BigDecimal serviceCharge, String paymentMethod) {
        Order existing = orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Order not found: " + id));
        Order updated = Order.builder()
                .id(existing.getId()).tenantId(existing.getTenantId()).outletId(existing.getOutletId())
                .tableId(existing.getTableId()).customerId(existing.getCustomerId())
                .orderNumber(existing.getOrderNumber()).type(existing.getType()).status("completed")
                .subtotal(existing.getSubtotal())
                .discount(discount != null ? discount : BigDecimal.ZERO)
                .taxAmount(existing.getTaxAmount())
                .serviceCharge(serviceCharge != null ? serviceCharge : BigDecimal.ZERO)
                .total(existing.getSubtotal().subtract(discount != null ? discount : BigDecimal.ZERO)
                        .add(existing.getTaxAmount()).add(serviceCharge != null ? serviceCharge : BigDecimal.ZERO))
                .paymentMethod(paymentMethod).paymentStatus("paid")
                .discountType(existing.getDiscountType())
                .notes(existing.getNotes()).servedBy(existing.getServedBy())
                .createdAt(existing.getCreatedAt()).completedAt(Instant.now())
                .build();
        return orderRepository.save(updated);
    }

    @Override
    public List<OrderItem> getOrderItems(UUID orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    private Order recalculateOrder(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
        BigDecimal subtotal = items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tax = subtotal.multiply(BigDecimal.valueOf(0.10));
        Order updated = Order.builder()
                .id(order.getId()).tenantId(order.getTenantId()).outletId(order.getOutletId())
                .tableId(order.getTableId()).customerId(order.getCustomerId())
                .orderNumber(order.getOrderNumber()).type(order.getType()).status(order.getStatus())
                .subtotal(subtotal).discount(order.getDiscount()).taxAmount(tax)
                .serviceCharge(order.getServiceCharge())
                .total(subtotal.add(tax)).paymentStatus(order.getPaymentStatus())
                .paymentMethod(order.getPaymentMethod()).discountType(order.getDiscountType())
                .notes(order.getNotes()).servedBy(order.getServedBy())
                .createdAt(order.getCreatedAt()).completedAt(order.getCompletedAt())
                .build();
        return orderRepository.save(updated);
    }
}

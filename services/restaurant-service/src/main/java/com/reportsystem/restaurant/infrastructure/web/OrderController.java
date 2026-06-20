package com.reportsystem.restaurant.infrastructure.web;

import com.reportsystem.restaurant.domain.model.Order;
import com.reportsystem.restaurant.domain.model.OrderItem;
import com.reportsystem.restaurant.domain.service.OrderServiceImpl;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderServiceImpl orderService;
    public OrderController(OrderServiceImpl orderService) { this.orderService = orderService; }

    @PostMapping
    public ResponseEntity<Order> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                        @RequestBody Map<String, Object> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(
                orderService.createOrder(
                        UUID.fromString((String)b.get("tenantId")),
                        bid,
                        UUID.fromString((String)b.get("outletId")),
                        b.get("tableId") != null ? UUID.fromString((String)b.get("tableId")) : null,
                        b.get("customerId") != null ? UUID.fromString((String)b.get("customerId")) : null,
                        (String)b.getOrDefault("type", "dine_in"),
                        (String)b.get("notes"),
                        b.get("servedBy") != null ? UUID.fromString((String)b.get("servedBy")) : null));
    }

    @PostMapping("/with-items")
    public ResponseEntity<Order> createWithItems(@RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                                 @RequestBody Map<String, Object> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawItems = (List<Map<String, Object>>) b.get("items");
        List<OrderItem> items = new ArrayList<>();
        if (rawItems != null) {
            for (Map<String, Object> raw : rawItems) {
                OrderItem item = OrderItem.builder()
                        .id(UUID.randomUUID())
                        .menuItemId(UUID.fromString((String) raw.get("menuItemId")))
                        .quantity((Integer) raw.get("quantity"))
                        .unitPrice(new BigDecimal(raw.get("unitPrice").toString()))
                        .modifiers((String) raw.get("modifiers"))
                        .build();
                items.add(item);
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(
                orderService.createOrderWithItems(
                        UUID.fromString((String)b.get("tenantId")),
                        bid,
                        UUID.fromString((String)b.get("outletId")),
                        b.get("tableId") != null ? UUID.fromString((String)b.get("tableId")) : null,
                        b.get("customerId") != null ? UUID.fromString((String)b.get("customerId")) : null,
                        (String)b.getOrDefault("type", "dine_in"),
                        (String)b.get("notes"),
                        b.get("servedBy") != null ? UUID.fromString((String)b.get("servedBy")) : null,
                        items));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<Order> addItem(@PathVariable UUID id, @RequestBody Map<String, Object> b) {
        return ResponseEntity.ok(orderService.addItem(id,
                UUID.fromString((String)b.get("menuItemId")),
                (int)b.get("quantity"),
                new BigDecimal(b.get("unitPrice").toString()),
                (String)b.get("modifiers")));
    }

    @DeleteMapping("/{orderId}/items/{itemId}")
    public ResponseEntity<Order> removeItem(@PathVariable UUID orderId, @PathVariable UUID itemId) {
        return ResponseEntity.ok(orderService.removeItem(orderId, itemId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> get(@PathVariable UUID id) {
        return orderService.getOrderById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-outlet/{outletId}")
    public ResponseEntity<List<Order>> getByOutlet(@PathVariable UUID outletId,
                                                    @RequestParam(defaultValue = "pending") String status) {
        return ResponseEntity.ok(orderService.getOrdersByOutlet(outletId, status));
    }

    @GetMapping("/by-tenant/{tenantId}")
    public ResponseEntity<List<Order>> getByTenant(@PathVariable UUID tenantId,
                                                   @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.ok(orderService.getOrdersByTenantAndBranch(tenantId, bid));
    }

    @GetMapping("/{id}/items")
    public ResponseEntity<List<OrderItem>> getItems(@PathVariable UUID id) {
        return ResponseEntity.ok(orderService.getOrderItems(id));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable UUID id, @RequestBody Map<String, String> b) {
        return ResponseEntity.ok(orderService.updateStatus(id, b.get("status")));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<Order> complete(@PathVariable UUID id, @RequestBody Map<String, Object> b) {
        return ResponseEntity.ok(orderService.completeOrder(id,
                b.get("discount") != null ? new BigDecimal(b.get("discount").toString()) : null,
                b.get("serviceCharge") != null ? new BigDecimal(b.get("serviceCharge").toString()) : null,
                (String)b.get("paymentMethod")));
    }
}

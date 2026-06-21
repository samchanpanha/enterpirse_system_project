package com.reportsystem.restaurant.infrastructure.web;

import com.reportsystem.restaurant.domain.model.MenuItem;
import com.reportsystem.restaurant.domain.model.Order;
import com.reportsystem.restaurant.domain.model.PosCart;
import com.reportsystem.restaurant.domain.port.inbound.MenuService;
import com.reportsystem.restaurant.domain.port.inbound.OrderService;
import com.reportsystem.restaurant.domain.port.inbound.PosService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pos")
public class PosController {

    private final PosService posService;
    private final OrderService orderService;
    private final MenuService menuService;
    private final Map<UUID, PosCart> carts = new ConcurrentHashMap<>();

    public PosController(PosService posService, OrderService orderService, MenuService menuService) {
        this.posService = posService;
        this.orderService = orderService;
        this.menuService = menuService;
    }

    @PostMapping("/{outletId}/cart")
    public ResponseEntity<PosCart> createCart(@PathVariable UUID outletId,
                                               @RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                               @RequestBody Map<String, Object> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        UUID tableId = b.get("tableId") != null ? UUID.fromString((String) b.get("tableId")) : null;
        UUID createdBy = b.get("createdBy") != null ? UUID.fromString((String) b.get("createdBy")) : null;
        UUID tenantId = UUID.fromString((String) b.get("tenantId"));
        PosCart cart = new PosCart(tenantId, bid, outletId, tableId, createdBy);
        carts.put(cart.getId(), cart);
        return ResponseEntity.status(HttpStatus.CREATED).body(cart);
    }

    @PostMapping("/{outletId}/cart/{id}/items")
    public ResponseEntity<PosCart> addItem(@PathVariable UUID outletId, @PathVariable UUID id,
                                            @RequestBody Map<String, Object> b) {
        PosCart cart = carts.get(id);
        if (cart == null) return ResponseEntity.notFound().build();
        UUID menuItemId = UUID.fromString((String) b.get("menuItemId"));
        int quantity = (int) b.get("quantity");
        String modifiers = (String) b.get("modifiers");
        Optional<MenuItem> menuItem = menuService.getMenuItemById(menuItemId);
        if (menuItem.isEmpty()) return ResponseEntity.badRequest().build();
        cart.addItem(menuItemId, menuItem.get().getName(), menuItem.get().getPrice(), quantity, modifiers);
        return ResponseEntity.ok(cart);
    }

    @DeleteMapping("/{outletId}/cart/{id}/items/{itemId}")
    public ResponseEntity<PosCart> removeItem(@PathVariable UUID outletId, @PathVariable UUID id,
                                               @PathVariable UUID itemId) {
        PosCart cart = carts.get(id);
        if (cart == null) return ResponseEntity.notFound().build();
        cart.removeItem(itemId);
        return ResponseEntity.ok(cart);
    }

    @PutMapping("/{outletId}/cart/{id}/discount")
    public ResponseEntity<PosCart> applyDiscount(@PathVariable UUID outletId, @PathVariable UUID id,
                                                  @RequestBody Map<String, Object> b) {
        PosCart cart = carts.get(id);
        if (cart == null) return ResponseEntity.notFound().build();
        BigDecimal discount = new BigDecimal(b.get("discount").toString());
        String type = (String) b.getOrDefault("type", "fixed");
        cart.setDiscount(discount);
        cart.setDiscountType(type);
        return ResponseEntity.ok(cart);
    }

    @PostMapping("/{outletId}/cart/{id}/split")
    public ResponseEntity<List<PosCart>> splitCart(@PathVariable UUID outletId, @PathVariable UUID id,
                                                    @RequestBody Map<String, Object> b) {
        PosCart cart = carts.get(id);
        if (cart == null) return ResponseEntity.notFound().build();
        @SuppressWarnings("unchecked")
        List<List<String>> splits = (List<List<String>>) b.get("splits");
        if (splits == null || splits.isEmpty()) return ResponseEntity.badRequest().build();
        List<PosCart> splitCarts = new ArrayList<>();
        for (List<String> splitItemIds : splits) {
            PosCart split = new PosCart(cart.getTenantId(), cart.getBranchId(), outletId, cart.getTableId(), cart.getCreatedBy());
            for (String itemIdStr : splitItemIds) {
                UUID itemId = UUID.fromString(itemIdStr);
                cart.getItems().stream()
                        .filter(i -> i.getId().equals(itemId))
                        .findFirst()
                        .ifPresent(item -> split.addItem(item.getMenuItemId(), item.getItemName(), item.getUnitPrice(), item.getQuantity(), item.getModifiers()));
            }
            if (!split.getItems().isEmpty()) {
                carts.put(split.getId(), split);
                splitCarts.add(split);
            }
        }
        if (!splitCarts.isEmpty()) {
            cart.clearItems();
            cart.setStatus("split");
        }
        return ResponseEntity.ok(splitCarts);
    }

    @PostMapping("/{outletId}/cart/{id}/payment")
    public ResponseEntity<Order> processPayment(@PathVariable UUID outletId, @PathVariable UUID id,
                                                 @RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                                 @RequestBody Map<String, Object> b) {
        PosCart cart = carts.get(id);
        if (cart == null) return ResponseEntity.notFound().build();
        BigDecimal amountTendered = new BigDecimal(b.get("amountTendered").toString());
        String paymentMethod = (String) b.getOrDefault("paymentMethod", "cash");
        List<PosService.PosOrderItem> items = cart.getItems().stream()
                .map(i -> new PosService.PosOrderItem(i.getMenuItemId(), i.getQuantity(), i.getModifiers()))
                .toList();
        Order order = posService.createPosOrder(cart.getTenantId(), cart.getBranchId(), outletId, cart.getTableId(), items);
        order = orderService.updateOrderDiscount(order.getId(), cart.getDiscount(), cart.getDiscountType());
        order = posService.completePosOrder(order.getId(), amountTendered, paymentMethod);
        carts.remove(id);
        return ResponseEntity.ok(order);
    }

    @GetMapping("/{outletId}/barcode/{code}")
    public ResponseEntity<MenuItem> barcodeLookup(@PathVariable UUID outletId, @PathVariable String code) {
        return menuService.getMenuItemById(UUID.fromString(code))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{outletId}/quick-sale")
    public ResponseEntity<Order> quickSale(@PathVariable UUID outletId,
                                            @RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                            @RequestBody Map<String, Object> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        UUID tenantId = UUID.fromString((String) b.get("tenantId"));
        BigDecimal amount = new BigDecimal(b.get("amount").toString());
        String paymentMethod = (String) b.getOrDefault("paymentMethod", "cash");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> rawItems = (List<Map<String, Object>>) b.get("items");
        List<PosService.PosOrderItem> items = new ArrayList<>();
        if (rawItems != null) {
            for (Map<String, Object> raw : rawItems) {
                items.add(new PosService.PosOrderItem(
                        UUID.fromString((String) raw.get("menuItemId")),
                        (int) raw.get("quantity"),
                        (String) raw.get("modifiers")));
            }
        }
        Order order = posService.createPosOrder(tenantId, bid, outletId, null, items);
        return ResponseEntity.ok(posService.completePosOrder(order.getId(), amount, paymentMethod));
    }

    @GetMapping("/{outletId}/x-report")
    public ResponseEntity<Map<String, Object>> xReport(@PathVariable UUID outletId) {
        List<Order> orders = orderService.getOrdersByOutlet(outletId, "completed");
        Map<String, Object> report = generateReport(orders);
        return ResponseEntity.ok(report);
    }

    @GetMapping("/{outletId}/z-report")
    public ResponseEntity<Map<String, Object>> zReport(@PathVariable UUID outletId) {
        List<Order> orders = orderService.getOrdersByOutlet(outletId, "completed");
        Map<String, Object> report = generateReport(orders);
        report.put("type", "Z-REPORT");
        report.put("note", "This report resets daily counters");
        return ResponseEntity.ok(report);
    }

    @PostMapping("/kds/{outletId}/bump/{orderItemId}")
    public ResponseEntity<Void> bumpItem(@PathVariable UUID outletId, @PathVariable UUID orderItemId) {
        orderService.updateOrderItemStatus(orderItemId, "SERVED");
        return ResponseEntity.ok().build();
    }

    @PostMapping("/kds/{outletId}/recall/{orderItemId}")
    public ResponseEntity<Void> recallItem(@PathVariable UUID outletId, @PathVariable UUID orderItemId) {
        orderService.updateOrderItemStatus(orderItemId, "RECALLED");
        return ResponseEntity.ok().build();
    }

    @GetMapping("/receipt/{orderId}")
    public ResponseEntity<Map<String, Object>> getReceipt(@PathVariable UUID orderId) {
        return orderService.getOrderById(orderId)
                .map(order -> {
                    Map<String, Object> receipt = new LinkedHashMap<>();
                    receipt.put("orderId", order.getId());
                    receipt.put("orderNumber", order.getOrderNumber());
                    receipt.put("outletId", order.getOutletId());
                    receipt.put("tableId", order.getTableId());
                    receipt.put("status", order.getStatus());
                    receipt.put("subtotal", order.getSubtotal());
                    receipt.put("discount", order.getDiscount());
                    receipt.put("serviceCharge", order.getServiceCharge());
                    receipt.put("taxAmount", order.getTaxAmount());
                    receipt.put("totalAmount", order.getTotal());
                    receipt.put("paymentMethod", order.getPaymentMethod() != null ? order.getPaymentMethod() : "");
                    receipt.put("createdAt", order.getCreatedAt());
                    receipt.put("items", orderService.getOrderItems(order.getId()));
                    return ResponseEntity.ok(receipt);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private Map<String, Object> generateReport(List<Order> orders) {
        BigDecimal totalSales = orders.stream()
                .map(o -> o.getTotal() != null ? o.getTotal() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long totalOrders = orders.size();
        Map<String, Long> paymentMethodCount = new HashMap<>();
        orders.forEach(o -> {
            if (o.getPaymentMethod() != null) {
                paymentMethodCount.merge(o.getPaymentMethod(), 1L, Long::sum);
            }
        });
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("type", "X-REPORT");
        report.put("date", LocalDate.now().toString());
        report.put("totalOrders", totalOrders);
        report.put("totalSales", totalSales);
        report.put("averageOrderValue", totalOrders > 0 ? totalSales.divide(BigDecimal.valueOf(totalOrders), BigDecimal.ROUND_HALF_UP) : BigDecimal.ZERO);
        report.put("paymentMethods", paymentMethodCount);
        return report;
    }
}

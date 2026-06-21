package com.reportsystem.delivery.infrastructure.web;

import com.reportsystem.delivery.domain.model.Delivery;
import com.reportsystem.delivery.domain.service.DeliveryServiceImpl;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/deliveries")
public class DeliveryController {

    private final DeliveryServiceImpl deliveryService;
    public DeliveryController(DeliveryServiceImpl deliveryService) { this.deliveryService = deliveryService; }

    @PostMapping
    public ResponseEntity<Delivery> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                           @RequestBody Map<String, Object> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(
                deliveryService.createDelivery(
                        UUID.fromString((String)b.get("tenantId")), bid,
                        b.get("outletId") != null ? UUID.fromString((String)b.get("outletId")) : null,
                        b.get("orderId") != null ? UUID.fromString((String)b.get("orderId")) : null,
                        b.get("driverId") != null ? UUID.fromString((String)b.get("driverId")) : null,
                        (String)b.get("customerName"), (String)b.get("customerPhone"),
                        (String)b.get("deliveryAddress"), (String)b.get("pickupAddress")));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Delivery> get(@PathVariable UUID id) {
        return deliveryService.getDeliveryById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-tenant/{tenantId}")
    public ResponseEntity<List<Delivery>> getByTenant(@PathVariable UUID tenantId,
                                                      @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(deliveryService.getDeliveriesByTenantAndBranch(tenantId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(deliveryService.getDeliveriesByTenant(tenantId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Delivery> updateStatus(@PathVariable UUID id, @RequestBody Map<String, String> b) {
        return ResponseEntity.ok(deliveryService.updateStatus(id, b.get("status")));
    }
}

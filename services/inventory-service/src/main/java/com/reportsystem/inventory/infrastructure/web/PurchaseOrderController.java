package com.reportsystem.inventory.infrastructure.web;

import com.reportsystem.inventory.domain.model.*;
import com.reportsystem.inventory.domain.port.inbound.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/purchase-orders")
public class PurchaseOrderController {
    private final PurchaseOrderService poService;
    public PurchaseOrderController(PurchaseOrderService p) { this.poService = p; }
    @PostMapping public ResponseEntity<PurchaseOrder> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId, @RequestBody Map<String, Object> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(poService.createPO(
                UUID.fromString((String)b.get("tenantId")),
                bid,
                UUID.fromString((String)b.get("supplierId")),
                java.time.LocalDate.parse((String)b.get("orderDate"))));
    }
    @GetMapping("/{id}") public ResponseEntity<PurchaseOrder> get(@PathVariable UUID id) {
        return poService.getPOById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/by-tenant/{tenantId}") public ResponseEntity<List<PurchaseOrder>> getByTenant(@PathVariable UUID tenantId, @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(poService.getPOsByTenantAndBranch(tenantId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(poService.getPOsByTenant(tenantId));
    }
    @PostMapping("/{id}/items") public ResponseEntity<PurchaseOrder> addItem(@PathVariable UUID id, @RequestBody Map<String, Object> b) {
        return ResponseEntity.ok(poService.addItem(id,
                UUID.fromString((String)b.get("productId")),
                new java.math.BigDecimal(b.get("quantity").toString()),
                new java.math.BigDecimal(b.get("unitCost").toString())));
    }
    @PostMapping("/{id}/receive") public ResponseEntity<PurchaseOrder> receive(@PathVariable UUID id) {
        return ResponseEntity.ok(poService.receiveItems(id));
    }
}
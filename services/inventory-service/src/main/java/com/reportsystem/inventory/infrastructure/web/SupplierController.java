package com.reportsystem.inventory.infrastructure.web;

import com.reportsystem.inventory.domain.model.*;
import com.reportsystem.inventory.domain.port.inbound.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/suppliers")
public class SupplierController {
    private final SupplierService supplierService;
    public SupplierController(SupplierService s) { this.supplierService = s; }
    @PostMapping public ResponseEntity<Supplier> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId, @RequestBody Map<String, String> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.createSupplier(
                UUID.fromString(b.get("tenantId")), bid, b.get("name"), b.get("phone")));
    }
    @GetMapping("/{id}") public ResponseEntity<Supplier> get(@PathVariable UUID id) {
        return supplierService.getSupplierById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/by-tenant/{tenantId}") public ResponseEntity<List<Supplier>> getByTenant(@PathVariable UUID tenantId, @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(supplierService.getSuppliersByTenantAndBranch(tenantId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(supplierService.getSuppliersByTenant(tenantId));
    }
}
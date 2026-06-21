package com.reportsystem.inventory.infrastructure.web;

import com.reportsystem.inventory.domain.model.*;
import com.reportsystem.inventory.domain.port.inbound.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/products")
public class ProductController {
    private final ProductService productService;
    public ProductController(ProductService p) { this.productService = p; }
    @PostMapping public ResponseEntity<Product> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId, @RequestBody Map<String, Object> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.createProduct(
                UUID.fromString((String)b.get("tenantId")),
                bid,
                b.get("categoryId") != null ? UUID.fromString((String)b.get("categoryId")) : null,
                (String)b.get("name"), (String)b.get("sku"),
                new java.math.BigDecimal(b.get("unitPrice").toString()),
                b.get("costPrice") != null ? new java.math.BigDecimal(b.get("costPrice").toString()) : null));
    }
    @GetMapping("/barcode/{barcode}") public ResponseEntity<Product> getByBarcode(@PathVariable String barcode) {
        return productService.getProductByBarcode(barcode).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/{id}") public ResponseEntity<Product> get(@PathVariable UUID id) {
        return productService.getProductById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/by-tenant/{tenantId}") public ResponseEntity<List<Product>> getByTenant(@PathVariable UUID tenantId, @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(productService.getProductsByTenantAndBranch(tenantId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(productService.getProductsByTenant(tenantId));
    }
    @PutMapping("/{id}") public ResponseEntity<Product> update(@PathVariable UUID id, @RequestBody Map<String, Object> b) {
        return ResponseEntity.ok(productService.updateProduct(id, (String)b.get("name"),
                b.get("unitPrice") != null ? new java.math.BigDecimal(b.get("unitPrice").toString()) : null,
                (boolean)b.getOrDefault("active", true)));
    }
}
package com.reportsystem.inventory.infrastructure.web;

import com.reportsystem.inventory.domain.model.*;
import com.reportsystem.inventory.domain.port.inbound.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/stock")
public class StockController {
    private final StockService stockService;
    public StockController(StockService s) { this.stockService = s; }
    @PostMapping("/entry") public ResponseEntity<StockEntry> addEntry(@RequestHeader(value = "X-Branch-Id", required = false) String branchId, @RequestBody Map<String, Object> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(stockService.addStock(
                UUID.fromString((String)b.get("tenantId")),
                bid,
                UUID.fromString((String)b.get("warehouseId")),
                UUID.fromString((String)b.get("productId")),
                new java.math.BigDecimal(b.get("quantity").toString()),
                b.get("unitCost") != null ? new java.math.BigDecimal(b.get("unitCost").toString()) : null,
                (String)b.get("batchNumber"), (String)b.get("notes")));
    }
    @PostMapping("/exit") public ResponseEntity<StockExit> removeExit(@RequestHeader(value = "X-Branch-Id", required = false) String branchId, @RequestBody Map<String, Object> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(stockService.removeStock(
                UUID.fromString((String)b.get("tenantId")),
                bid,
                UUID.fromString((String)b.get("warehouseId")),
                UUID.fromString((String)b.get("productId")),
                new java.math.BigDecimal(b.get("quantity").toString()),
                (String)b.get("referenceType"),
                b.get("referenceId") != null ? UUID.fromString((String)b.get("referenceId")) : null,
                (String)b.get("notes")));
    }
    @GetMapping("/current/{tenantId}/{productId}") public ResponseEntity<Map<String, Object>> getStock(@RequestHeader(value = "X-Branch-Id", required = false) String branchId, @PathVariable UUID tenantId, @PathVariable UUID productId) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.ok(Map.of("productId", productId, "quantity", stockService.getCurrentStock(tenantId, bid, productId)));
    }
}
package com.reportsystem.restaurant.infrastructure.web;

import com.reportsystem.restaurant.domain.model.Outlet;
import com.reportsystem.restaurant.domain.port.inbound.OutletService;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/outlets")
public class OutletController {

    private final OutletService outletService;
    public OutletController(OutletService outletService) { this.outletService = outletService; }

    @PostMapping
    public ResponseEntity<Outlet> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                         @RequestBody Map<String, String> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(
                outletService.createOutlet(UUID.fromString(b.get("tenantId")), bid, b.get("name")));
    }
    @GetMapping("/{id}")
    public ResponseEntity<Outlet> get(@PathVariable UUID id) {
        return outletService.getOutletById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/by-tenant/{tenantId}")
    public ResponseEntity<List<Outlet>> getByTenant(@PathVariable UUID tenantId,
                                                    @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(outletService.getOutletsByTenantAndBranch(tenantId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(outletService.getOutletsByTenant(tenantId));
    }
}

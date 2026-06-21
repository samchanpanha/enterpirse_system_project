package com.reportsystem.delivery.infrastructure.web;

import com.reportsystem.delivery.domain.model.Driver;
import com.reportsystem.delivery.domain.service.DriverServiceImpl;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/drivers")
public class DriverController {

    private final DriverServiceImpl driverService;
    public DriverController(DriverServiceImpl driverService) { this.driverService = driverService; }

    @PostMapping
    public ResponseEntity<Driver> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                         @RequestBody Map<String, String> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(
                driverService.createDriver(UUID.fromString(b.get("tenantId")), bid,
                        b.get("name"), b.get("phone"), b.get("email")));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Driver> get(@PathVariable UUID id) {
        return driverService.getDriverById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-tenant/{tenantId}")
    public ResponseEntity<List<Driver>> getByTenant(@PathVariable UUID tenantId,
                                                    @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(driverService.getDriversByTenantAndBranch(tenantId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(driverService.getDriversByTenant(tenantId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Driver> updateStatus(@PathVariable UUID id, @RequestBody Map<String, String> b) {
        return ResponseEntity.ok(driverService.updateStatus(id, b.get("status")));
    }
}

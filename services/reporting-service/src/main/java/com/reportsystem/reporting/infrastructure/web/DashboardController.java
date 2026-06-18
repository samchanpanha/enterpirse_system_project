package com.reportsystem.reporting.infrastructure.web;

import com.reportsystem.reporting.domain.model.*;
import com.reportsystem.reporting.domain.port.inbound.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/dashboards")
public class DashboardController {
    private final DashboardService dashService;
    public DashboardController(DashboardService d) { dashService = d; }
    @PostMapping public ResponseEntity<DashboardConfig> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId, @RequestBody Map<String, Object> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(dashService.createDashboard(
                UUID.fromString((String)b.get("tenantId")), bid, (String)b.get("name"),
                (String)b.get("layout"),
                b.get("createdBy") != null ? UUID.fromString((String)b.get("createdBy")) : null));
    }
    @GetMapping("/by-tenant/{tenantId}") public ResponseEntity<List<DashboardConfig>> getByTenant(@PathVariable UUID tenantId, @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(dashService.getDashboardsByTenantAndBranch(tenantId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(dashService.getDashboardsByTenant(tenantId));
    }
}
package com.reportsystem.reporting.infrastructure.web;

import com.reportsystem.reporting.domain.model.*;
import com.reportsystem.reporting.domain.port.inbound.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/dashboards")
@Tag(name = "Dashboard Management", description = "APIs for managing dashboard configurations")
public class DashboardController {
    private final DashboardService dashService;
    public DashboardController(DashboardService d) { dashService = d; }
    
    @PostMapping
    @Operation(summary = "Create a new dashboard", description = "Creates a new dashboard configuration with the specified layout")
    public ResponseEntity<DashboardConfig> create(
            @Parameter(description = "Branch ID header") @RequestHeader(value = "X-Branch-Id", required = false) String branchId,
            @Valid @RequestBody Map<String, Object> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(dashService.createDashboard(
                UUID.fromString((String)b.get("tenantId")), bid, (String)b.get("name"),
                (String)b.get("layout"),
                b.get("createdBy") != null ? UUID.fromString((String)b.get("createdBy")) : null));
    }
    
    @GetMapping("/by-tenant/{tenantId}")
    @Operation(summary = "Get dashboards by tenant", description = "Retrieves all dashboard configurations for a specific tenant")
    public ResponseEntity<List<DashboardConfig>> getByTenant(
            @Parameter(description = "Tenant ID") @PathVariable UUID tenantId,
            @Parameter(description = "Optional branch ID filter") @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(dashService.getDashboardsByTenantAndBranch(tenantId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(dashService.getDashboardsByTenant(tenantId));
    }
}
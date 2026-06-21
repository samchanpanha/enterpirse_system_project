package com.reportsystem.delivery.infrastructure.web;

import com.reportsystem.delivery.domain.model.FleetVehicle;
import com.reportsystem.delivery.domain.service.FleetServiceImpl;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/fleet-vehicles")
public class FleetController {

    private final FleetServiceImpl fleetService;
    public FleetController(FleetServiceImpl fleetService) { this.fleetService = fleetService; }

    @PostMapping
    public ResponseEntity<FleetVehicle> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                               @RequestBody Map<String, String> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(
                fleetService.createVehicle(UUID.fromString(b.get("tenantId")), bid,
                        b.get("name"), b.get("plateNumber"), b.get("vehicleType")));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FleetVehicle> get(@PathVariable UUID id) {
        return fleetService.getVehicleById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-tenant/{tenantId}")
    public ResponseEntity<List<FleetVehicle>> getByTenant(@PathVariable UUID tenantId,
                                                         @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(fleetService.getVehiclesByTenantAndBranch(tenantId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(fleetService.getVehiclesByTenant(tenantId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<FleetVehicle> updateStatus(@PathVariable UUID id, @RequestBody Map<String, String> b) {
        return ResponseEntity.ok(fleetService.updateStatus(id, b.get("status")));
    }
}

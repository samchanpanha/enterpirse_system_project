package com.reportsystem.delivery.infrastructure.web;

import com.reportsystem.delivery.domain.model.DeliveryZone;
import com.reportsystem.delivery.domain.service.ZoneServiceImpl;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/delivery-zones")
public class ZoneController {

    private final ZoneServiceImpl zoneService;
    public ZoneController(ZoneServiceImpl zoneService) { this.zoneService = zoneService; }

    @PostMapping
    public ResponseEntity<DeliveryZone> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                               @RequestBody Map<String, String> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(
                zoneService.createZone(UUID.fromString(b.get("tenantId")), bid,
                        b.get("name"), b.get("nameKh")));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DeliveryZone> get(@PathVariable UUID id) {
        return zoneService.getZoneById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-tenant/{tenantId}")
    public ResponseEntity<List<DeliveryZone>> getByTenant(@PathVariable UUID tenantId,
                                                         @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(zoneService.getZonesByTenantAndBranch(tenantId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(zoneService.getZonesByTenant(tenantId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<DeliveryZone> updateStatus(@PathVariable UUID id, @RequestBody Map<String, Object> b) {
        boolean active = b.get("isActive") instanceof Boolean ? (Boolean) b.get("isActive") : Boolean.parseBoolean((String) b.get("isActive"));
        return ResponseEntity.ok(zoneService.updateStatus(id, active));
    }
}

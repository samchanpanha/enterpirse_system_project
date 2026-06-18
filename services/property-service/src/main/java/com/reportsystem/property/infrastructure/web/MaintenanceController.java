package com.reportsystem.property.infrastructure.web;

import com.reportsystem.property.domain.model.MaintenanceTicket;
import com.reportsystem.property.domain.service.MaintenanceService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/maintenance")
public class MaintenanceController {

    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @PostMapping
    public ResponseEntity<MaintenanceTicket> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                                    @RequestBody Map<String, String> body) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        MaintenanceTicket ticket = maintenanceService.createTicket(
                UUID.fromString(body.get("tenantId")),
                bid,
                UUID.fromString(body.get("unitId")),
                body.get("title"), body.get("description"),
                body.get("priority"), body.get("category"));
        return ResponseEntity.status(HttpStatus.CREATED).body(ticket);
    }

    @GetMapping("/{id}")
    public ResponseEntity<MaintenanceTicket> getById(@PathVariable UUID id) {
        return maintenanceService.getTicketById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-unit/{unitId}")
    public ResponseEntity<List<MaintenanceTicket>> getByUnit(@PathVariable UUID unitId) {
        return ResponseEntity.ok(maintenanceService.getTicketsByUnit(unitId));
    }

    @GetMapping("/by-property/{propertyId}")
    public ResponseEntity<List<MaintenanceTicket>> getByProperty(@PathVariable UUID propertyId) {
        return ResponseEntity.ok(maintenanceService.getTicketsByProperty(propertyId));
    }

    @GetMapping("/by-tenant/{tenantId}")
    public ResponseEntity<List<MaintenanceTicket>> getByTenant(@PathVariable UUID tenantId,
                                                               @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(maintenanceService.getTicketsByTenantAndBranch(tenantId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(maintenanceService.getTicketsByTenant(tenantId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<MaintenanceTicket> updateStatus(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(maintenanceService.updateStatus(id, body.get("status")));
    }

    @PostMapping("/{id}/assign")
    public ResponseEntity<MaintenanceTicket> assign(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(maintenanceService.assignTicket(id, body.get("assignedTo")));
    }

    @PostMapping("/{id}/complete")
    public ResponseEntity<MaintenanceTicket> complete(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        BigDecimal cost = body.get("actualCost") != null ? new BigDecimal(body.get("actualCost")) : null;
        return ResponseEntity.ok(maintenanceService.completeTicket(id, cost));
    }
}

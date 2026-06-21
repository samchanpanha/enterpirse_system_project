package com.reportsystem.realty.infrastructure.web;

import com.reportsystem.realty.domain.model.Visitor;
import com.reportsystem.realty.domain.port.inbound.VisitorService;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/visitors")
public class VisitorController {

    private final VisitorService visitorService;
    public VisitorController(VisitorService visitorService) { this.visitorService = visitorService; }

    @PostMapping
    public ResponseEntity<Visitor> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                          @RequestBody Map<String, String> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        UUID propertyId = b.containsKey("propertyId") ? UUID.fromString(b.get("propertyId")) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(
                visitorService.createVisitor(UUID.fromString(b.get("tenantId")), bid, propertyId, b.get("name")));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Visitor> get(@PathVariable UUID id) {
        return visitorService.getVisitorById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-tenant/{tenantId}")
    public ResponseEntity<List<Visitor>> getByTenant(@PathVariable UUID tenantId,
                                                     @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(visitorService.getVisitorsByTenantAndBranch(tenantId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(visitorService.getVisitorsByTenant(tenantId));
    }

    @GetMapping("/by-property/{propertyId}")
    public ResponseEntity<List<Visitor>> getByProperty(@PathVariable UUID propertyId) {
        return ResponseEntity.ok(visitorService.getVisitorsByProperty(propertyId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Visitor> update(@PathVariable UUID id, @RequestBody Visitor visitor) {
        return visitorService.getVisitorById(id).map(existing -> {
            Visitor updated = Visitor.builder()
                    .id(existing.getId()).tenantId(existing.getTenantId()).branchId(existing.getBranchId())
                    .propertyId(visitor.getPropertyId()).name(visitor.getName()).phone(visitor.getPhone())
                    .idNumber(visitor.getIdNumber()).vehiclePlate(visitor.getVehiclePlate())
                    .purpose(visitor.getPurpose()).checkIn(visitor.getCheckIn()).checkOut(visitor.getCheckOut())
                    .qrCode(visitor.getQrCode()).status(visitor.getStatus())
                    .createdAt(existing.getCreatedAt())
                    .build();
            return ResponseEntity.ok(visitorService.updateVisitor(updated));
        }).orElse(ResponseEntity.notFound().build());
    }
}

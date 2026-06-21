package com.reportsystem.realty.infrastructure.web;

import com.reportsystem.realty.domain.model.Resident;
import com.reportsystem.realty.domain.port.inbound.ResidentService;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/residents")
public class ResidentController {

    private final ResidentService residentService;
    public ResidentController(ResidentService residentService) { this.residentService = residentService; }

    @PostMapping
    public ResponseEntity<Resident> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                           @RequestBody Map<String, String> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        UUID propertyId = b.containsKey("propertyId") ? UUID.fromString(b.get("propertyId")) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(
                residentService.createResident(UUID.fromString(b.get("tenantId")), bid, propertyId, b.get("name")));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resident> get(@PathVariable UUID id) {
        return residentService.getResidentById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-tenant/{tenantId}")
    public ResponseEntity<List<Resident>> getByTenant(@PathVariable UUID tenantId,
                                                      @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(residentService.getResidentsByTenantAndBranch(tenantId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(residentService.getResidentsByTenant(tenantId));
    }

    @GetMapping("/by-property/{propertyId}")
    public ResponseEntity<List<Resident>> getByProperty(@PathVariable UUID propertyId) {
        return ResponseEntity.ok(residentService.getResidentsByProperty(propertyId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Resident> update(@PathVariable UUID id, @RequestBody Resident resident) {
        return residentService.getResidentById(id).map(existing -> {
            Resident updated = Resident.builder()
                    .id(existing.getId()).tenantId(existing.getTenantId()).branchId(existing.getBranchId())
                    .propertyId(resident.getPropertyId()).name(resident.getName()).nameKh(resident.getNameKh())
                    .phone(resident.getPhone()).email(resident.getEmail()).idNumber(resident.getIdNumber())
                    .moveInDate(resident.getMoveInDate()).moveOutDate(resident.getMoveOutDate())
                    .status(resident.getStatus()).emergencyContact(resident.getEmergencyContact())
                    .emergencyPhone(resident.getEmergencyPhone()).notes(resident.getNotes())
                    .createdAt(existing.getCreatedAt()).updatedAt(java.time.Instant.now())
                    .build();
            return ResponseEntity.ok(residentService.updateResident(updated));
        }).orElse(ResponseEntity.notFound().build());
    }
}

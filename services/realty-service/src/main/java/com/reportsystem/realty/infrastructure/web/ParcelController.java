package com.reportsystem.realty.infrastructure.web;

import com.reportsystem.realty.domain.model.Parcel;
import com.reportsystem.realty.domain.port.inbound.ParcelService;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/parcels")
public class ParcelController {

    private final ParcelService parcelService;
    public ParcelController(ParcelService parcelService) { this.parcelService = parcelService; }

    @PostMapping
    public ResponseEntity<Parcel> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                         @RequestBody Map<String, String> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        UUID residentId = b.containsKey("residentId") ? UUID.fromString(b.get("residentId")) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(
                parcelService.createParcel(UUID.fromString(b.get("tenantId")), bid, residentId, b.get("carrier")));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Parcel> get(@PathVariable UUID id) {
        return parcelService.getParcelById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-tenant/{tenantId}")
    public ResponseEntity<List<Parcel>> getByTenant(@PathVariable UUID tenantId,
                                                    @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(parcelService.getParcelsByTenantAndBranch(tenantId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(parcelService.getParcelsByTenant(tenantId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Parcel> update(@PathVariable UUID id, @RequestBody Parcel parcel) {
        return parcelService.getParcelById(id).map(existing -> {
            Parcel updated = Parcel.builder()
                    .id(existing.getId()).tenantId(existing.getTenantId()).branchId(existing.getBranchId())
                    .residentId(parcel.getResidentId()).carrier(parcel.getCarrier())
                    .trackingNumber(parcel.getTrackingNumber()).description(parcel.getDescription())
                    .status(parcel.getStatus()).receivedAt(parcel.getReceivedAt())
                    .pickedUpAt(parcel.getPickedUpAt()).notes(parcel.getNotes())
                    .createdAt(existing.getCreatedAt())
                    .build();
            return ResponseEntity.ok(parcelService.updateParcel(updated));
        }).orElse(ResponseEntity.notFound().build());
    }
}

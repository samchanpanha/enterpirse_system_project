package com.reportsystem.realty.infrastructure.web;

import com.reportsystem.realty.domain.model.AmenityBooking;
import com.reportsystem.realty.domain.port.inbound.AmenityService;
import java.time.LocalDate;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/amenity-bookings")
public class AmenityController {

    private final AmenityService amenityService;
    public AmenityController(AmenityService amenityService) { this.amenityService = amenityService; }

    @PostMapping
    public ResponseEntity<AmenityBooking> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                                 @RequestBody Map<String, String> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        UUID residentId = UUID.fromString(b.get("residentId"));
        LocalDate bookedDate = LocalDate.parse(b.get("bookedDate"));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                amenityService.createBooking(UUID.fromString(b.get("tenantId")), bid, residentId, b.get("amenityType"), bookedDate));
    }

    @GetMapping("/{id}")
    public ResponseEntity<AmenityBooking> get(@PathVariable UUID id) {
        return amenityService.getBookingById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-tenant/{tenantId}")
    public ResponseEntity<List<AmenityBooking>> getByTenant(@PathVariable UUID tenantId,
                                                            @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(amenityService.getBookingsByTenantAndBranch(tenantId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(amenityService.getBookingsByTenant(tenantId));
    }

    @GetMapping("/by-resident/{residentId}")
    public ResponseEntity<List<AmenityBooking>> getByResident(@PathVariable UUID residentId) {
        return ResponseEntity.ok(amenityService.getBookingsByResident(residentId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AmenityBooking> update(@PathVariable UUID id, @RequestBody AmenityBooking booking) {
        return amenityService.getBookingById(id).map(existing -> {
            AmenityBooking updated = AmenityBooking.builder()
                    .id(existing.getId()).tenantId(existing.getTenantId()).branchId(existing.getBranchId())
                    .residentId(booking.getResidentId()).amenityType(booking.getAmenityType())
                    .bookedDate(booking.getBookedDate()).startTime(booking.getStartTime())
                    .endTime(booking.getEndTime()).guests(booking.getGuests())
                    .status(booking.getStatus()).notes(booking.getNotes())
                    .createdAt(existing.getCreatedAt()).updatedAt(java.time.Instant.now())
                    .build();
            return ResponseEntity.ok(amenityService.updateBooking(updated));
        }).orElse(ResponseEntity.notFound().build());
    }
}

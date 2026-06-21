package com.reportsystem.realty.infrastructure.web;

import com.reportsystem.realty.domain.model.HoaFee;
import com.reportsystem.realty.domain.port.inbound.HoaService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hoa-fees")
public class HoaController {

    private final HoaService hoaService;
    public HoaController(HoaService hoaService) { this.hoaService = hoaService; }

    @PostMapping
    public ResponseEntity<HoaFee> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                         @RequestBody Map<String, String> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        UUID propertyId = UUID.fromString(b.get("propertyId"));
        UUID residentId = b.containsKey("residentId") ? UUID.fromString(b.get("residentId")) : null;
        BigDecimal amount = new BigDecimal(b.get("amount"));
        LocalDate dueDate = LocalDate.parse(b.get("dueDate"));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                hoaService.createHoaFee(UUID.fromString(b.get("tenantId")), bid, propertyId, residentId, amount, dueDate));
    }

    @GetMapping("/{id}")
    public ResponseEntity<HoaFee> get(@PathVariable UUID id) {
        return hoaService.getHoaFeeById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-tenant/{tenantId}")
    public ResponseEntity<List<HoaFee>> getByTenant(@PathVariable UUID tenantId,
                                                    @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(hoaService.getHoaFeesByTenantAndBranch(tenantId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(hoaService.getHoaFeesByTenant(tenantId));
    }

    @GetMapping("/by-property/{propertyId}")
    public ResponseEntity<List<HoaFee>> getByProperty(@PathVariable UUID propertyId) {
        return ResponseEntity.ok(hoaService.getHoaFeesByProperty(propertyId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<HoaFee> update(@PathVariable UUID id, @RequestBody HoaFee hoaFee) {
        return hoaService.getHoaFeeById(id).map(existing -> {
            HoaFee updated = HoaFee.builder()
                    .id(existing.getId()).tenantId(existing.getTenantId()).branchId(existing.getBranchId())
                    .propertyId(hoaFee.getPropertyId()).residentId(hoaFee.getResidentId())
                    .amount(hoaFee.getAmount()).dueDate(hoaFee.getDueDate()).period(hoaFee.getPeriod())
                    .status(hoaFee.getStatus()).paidAt(hoaFee.getPaidAt()).paymentRef(hoaFee.getPaymentRef())
                    .lateFee(hoaFee.getLateFee()).notes(hoaFee.getNotes())
                    .createdAt(existing.getCreatedAt()).updatedAt(java.time.Instant.now())
                    .build();
            return ResponseEntity.ok(hoaService.updateHoaFee(updated));
        }).orElse(ResponseEntity.notFound().build());
    }
}

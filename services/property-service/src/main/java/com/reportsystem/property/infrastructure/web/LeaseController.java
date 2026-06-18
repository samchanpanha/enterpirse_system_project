package com.reportsystem.property.infrastructure.web;

import com.reportsystem.property.domain.model.Lease;
import com.reportsystem.property.domain.service.LeaseService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/leases")
public class LeaseController {

    private final LeaseService leaseService;

    public LeaseController(LeaseService leaseService) {
        this.leaseService = leaseService;
    }

    @PostMapping
    public ResponseEntity<Lease> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                        @RequestBody Map<String, String> body) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        Lease lease = leaseService.createLease(
                UUID.fromString(body.get("tenantId")),
                bid,
                UUID.fromString(body.get("unitId")),
                body.get("tenantName"), body.get("tenantPhone"),
                LocalDate.parse(body.get("startDate")),
                LocalDate.parse(body.get("endDate")),
                new BigDecimal(body.get("rentAmount")),
                body.get("depositAmount") != null ? new BigDecimal(body.get("depositAmount")) : null);
        return ResponseEntity.status(HttpStatus.CREATED).body(lease);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Lease> getById(@PathVariable UUID id) {
        return leaseService.getLeaseById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-unit/{unitId}")
    public ResponseEntity<List<Lease>> getByUnit(@PathVariable UUID unitId) {
        return ResponseEntity.ok(leaseService.getLeasesByUnit(unitId));
    }

    @GetMapping("/active/by-tenant/{tenantId}")
    public ResponseEntity<List<Lease>> getActiveByTenant(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(leaseService.getActiveLeasesByTenant(tenantId));
    }

    @PostMapping("/{id}/terminate")
    public ResponseEntity<Lease> terminate(@PathVariable UUID id) {
        return ResponseEntity.ok(leaseService.terminateLease(id));
    }

    @PostMapping("/{id}/renew")
    public ResponseEntity<Lease> renew(@PathVariable UUID id, @RequestBody Map<String, String> body) {
        LocalDate newEndDate = LocalDate.parse(body.get("newEndDate"));
        BigDecimal newRent = body.get("newRent") != null ? new BigDecimal(body.get("newRent")) : null;
        return ResponseEntity.ok(leaseService.renewLease(id, newEndDate, newRent));
    }
}

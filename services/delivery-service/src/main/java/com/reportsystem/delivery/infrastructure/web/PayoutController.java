package com.reportsystem.delivery.infrastructure.web;

import com.reportsystem.delivery.domain.model.DriverPayout;
import com.reportsystem.delivery.domain.service.PayoutServiceImpl;
import java.time.LocalDate;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/driver-payouts")
public class PayoutController {

    private final PayoutServiceImpl payoutService;
    public PayoutController(PayoutServiceImpl payoutService) { this.payoutService = payoutService; }

    @PostMapping
    public ResponseEntity<DriverPayout> create(@RequestBody Map<String, String> b) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                payoutService.createPayout(
                        UUID.fromString(b.get("tenantId")),
                        UUID.fromString(b.get("driverId")),
                        LocalDate.parse(b.get("periodStart")),
                        LocalDate.parse(b.get("periodEnd"))));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DriverPayout> get(@PathVariable UUID id) {
        return payoutService.getPayoutById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-tenant/{tenantId}")
    public ResponseEntity<List<DriverPayout>> getByTenant(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(payoutService.getPayoutsByTenant(tenantId));
    }

    @GetMapping("/by-driver/{driverId}")
    public ResponseEntity<List<DriverPayout>> getByDriver(@PathVariable UUID driverId) {
        return ResponseEntity.ok(payoutService.getPayoutsByDriver(driverId));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<DriverPayout> updateStatus(@PathVariable UUID id, @RequestBody Map<String, String> b) {
        return ResponseEntity.ok(payoutService.updateStatus(id, b.get("status")));
    }
}

package com.reportsystem.realty.infrastructure.web;

import com.reportsystem.realty.domain.model.PropertyValuation;
import com.reportsystem.realty.domain.port.inbound.ValuationService;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/valuations")
public class ValuationController {

    private final ValuationService valuationService;
    public ValuationController(ValuationService valuationService) { this.valuationService = valuationService; }

    @PostMapping
    public ResponseEntity<PropertyValuation> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                                    @RequestBody Map<String, String> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        UUID propertyId = UUID.fromString(b.get("propertyId"));
        BigDecimal value = new BigDecimal(b.get("estimatedValue"));
        return ResponseEntity.status(HttpStatus.CREATED).body(
                valuationService.createValuation(UUID.fromString(b.get("tenantId")), bid, propertyId, value));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyValuation> get(@PathVariable UUID id) {
        return valuationService.getValuationById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-tenant/{tenantId}")
    public ResponseEntity<List<PropertyValuation>> getByTenant(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(valuationService.getValuationsByTenant(tenantId));
    }

    @GetMapping("/by-property/{propertyId}")
    public ResponseEntity<List<PropertyValuation>> getByProperty(@PathVariable UUID propertyId) {
        return ResponseEntity.ok(valuationService.getValuationsByProperty(propertyId));
    }
}

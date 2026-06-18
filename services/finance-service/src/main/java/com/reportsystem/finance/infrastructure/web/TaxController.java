package com.reportsystem.finance.infrastructure.web;

import com.reportsystem.finance.domain.model.*;
import com.reportsystem.finance.domain.port.inbound.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/tax")
public class TaxController {
    private final TaxService taxService;
    public TaxController(TaxService t) { taxService = t; }
    @PostMapping("/records") public ResponseEntity<TaxRecord> record(@RequestHeader(value = "X-Branch-Id", required = false) String branchId, @RequestBody Map<String, Object> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(taxService.recordTax(
                UUID.fromString((String)b.get("tenantId")), bid, (String)b.get("taxType"),
                (int)b.get("periodMonth"), (int)b.get("periodYear"),
                new java.math.BigDecimal(b.get("taxableAmount").toString()),
                new java.math.BigDecimal(b.get("taxRate").toString()),
                (String)b.get("sourceType"),
                b.get("sourceId") != null ? UUID.fromString((String)b.get("sourceId")) : null));
    }
    @GetMapping("/records/{tenantId}/{taxType}/{year}/{month}")
    public ResponseEntity<List<TaxRecord>> getRecords(@PathVariable UUID tenantId, @PathVariable String taxType,
                                                       @PathVariable int year, @PathVariable int month) {
        return ResponseEntity.ok(taxService.getTaxRecords(tenantId, taxType, year, month));
    }
    @PostMapping("/reports/generate") public ResponseEntity<TaxFilingReport> generate(@RequestBody Map<String, Object> b) {
        return ResponseEntity.ok(taxService.generateTaxReport(
                UUID.fromString((String)b.get("tenantId")), (String)b.get("taxType"),
                (int)b.get("periodYear"), (int)b.get("periodMonth")));
    }
}
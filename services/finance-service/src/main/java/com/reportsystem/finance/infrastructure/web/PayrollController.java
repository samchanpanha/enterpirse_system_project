package com.reportsystem.finance.infrastructure.web;

import com.reportsystem.finance.domain.model.*;
import com.reportsystem.finance.domain.port.inbound.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/payroll")
public class PayrollController {
    private final PayrollService payrollService;
    public PayrollController(PayrollService p) { payrollService = p; }
    @PostMapping("/periods") public ResponseEntity<PayrollPeriod> createPeriod(@RequestHeader(value = "X-Branch-Id", required = false) String branchId, @RequestBody Map<String, Object> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(payrollService.createPayrollPeriod(
                UUID.fromString((String)b.get("tenantId")), bid, (int)b.get("periodMonth"), (int)b.get("periodYear")));
    }
    @PostMapping("/periods/{id}/run") public ResponseEntity<List<PayrollItem>> run(@PathVariable UUID id) {
        return ResponseEntity.ok(payrollService.runPayroll(id));
    }
}
package com.reportsystem.finance.infrastructure.web;

import com.reportsystem.finance.domain.model.*;
import com.reportsystem.finance.domain.port.inbound.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/employees")
public class EmployeeController {
    private final PayrollService payrollService;
    public EmployeeController(PayrollService p) { payrollService = p; }
    @PostMapping public ResponseEntity<Employee> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId, @RequestBody Map<String, Object> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(payrollService.createEmployee(
                UUID.fromString((String)b.get("tenantId")), bid, (String)b.get("firstName"),
                (String)b.get("lastName"),
                new java.math.BigDecimal(b.get("baseSalary").toString())));
    }
    @GetMapping("/by-tenant/{tenantId}") public ResponseEntity<List<Employee>> getByTenant(@PathVariable UUID tenantId, @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(payrollService.getEmployeesByTenantAndBranch(tenantId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(payrollService.getEmployeesByTenant(tenantId));
    }
}
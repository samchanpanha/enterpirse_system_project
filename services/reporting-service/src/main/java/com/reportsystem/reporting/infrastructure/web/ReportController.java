package com.reportsystem.reporting.infrastructure.web;

import com.reportsystem.reporting.domain.model.*;
import com.reportsystem.reporting.domain.port.inbound.*;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/reports")
public class ReportController {
    private final ReportService reportService;
    public ReportController(ReportService r) { reportService = r; }

    @PostMapping("/definitions")
    public ResponseEntity<ReportDefinition> createDef(@RequestHeader(value = "X-Branch-Id", required = false) String branchId, @RequestBody Map<String, Object> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.createDefinition(
                UUID.fromString((String)b.get("tenantId")), bid, (String)b.get("name"),
                (String)b.get("type"), (String)b.get("config")));
    }
    @GetMapping("/definitions/{id}") public ResponseEntity<ReportDefinition> getDef(@PathVariable UUID id) {
        return reportService.getDefinitionById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/definitions/by-tenant/{tenantId}") public ResponseEntity<List<ReportDefinition>> getDefs(@PathVariable UUID tenantId, @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(reportService.getDefinitionsByTenantAndBranch(tenantId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(reportService.getDefinitionsByTenant(tenantId));
    }
    @PostMapping("/definitions/{id}/execute")
    public ResponseEntity<ReportExecution> execute(@PathVariable UUID id, @RequestBody Map<String, Object> b) {
        return ResponseEntity.ok(reportService.executeReport(id,
                UUID.fromString((String)b.get("tenantId")),
                (String)b.get("parameters"),
                b.get("requestedBy") != null ? UUID.fromString((String)b.get("requestedBy")) : null));
    }
}
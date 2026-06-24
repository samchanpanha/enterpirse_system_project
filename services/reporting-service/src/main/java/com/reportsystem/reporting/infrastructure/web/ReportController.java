package com.reportsystem.reporting.infrastructure.web;

import com.reportsystem.reporting.domain.model.*;
import com.reportsystem.reporting.domain.port.inbound.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.*;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/reports")
@Tag(name = "Report Management", description = "APIs for managing report definitions and executions")
public class ReportController {
    private final ReportService reportService;
    public ReportController(ReportService r) { reportService = r; }

    @PostMapping("/definitions")
    @Operation(summary = "Create a new report definition", description = "Creates a new report definition with the specified configuration")
    public ResponseEntity<ReportDefinition> createDef(
            @Parameter(description = "Branch ID header") @RequestHeader(value = "X-Branch-Id", required = false) String branchId,
            @Valid @RequestBody Map<String, Object> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(reportService.createDefinition(
                UUID.fromString((String)b.get("tenantId")), bid, (String)b.get("name"),
                (String)b.get("type"), (String)b.get("config"), (String)b.get("layout")));
    }
    
    @GetMapping("/definitions/{id}")
    @Operation(summary = "Get report definition by ID", description = "Retrieves a specific report definition by its unique identifier")
    public ResponseEntity<ReportDefinition> getDef(@Parameter(description = "Report definition ID") @PathVariable UUID id) {
        return reportService.getDefinitionById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/definitions/by-tenant/{tenantId}")
    @Operation(summary = "Get definitions by tenant", description = "Retrieves all report definitions for a specific tenant")
    public ResponseEntity<List<ReportDefinition>> getDefs(
            @Parameter(description = "Tenant ID") @PathVariable UUID tenantId,
            @Parameter(description = "Optional branch ID filter") @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(reportService.getDefinitionsByTenantAndBranch(tenantId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(reportService.getDefinitionsByTenant(tenantId));
    }
    
    @PostMapping("/definitions/{id}/execute")
    @Operation(summary = "Execute a report", description = "Executes a report definition and returns the execution result")
    public ResponseEntity<ReportExecution> execute(
            @Parameter(description = "Report definition ID to execute") @PathVariable UUID id,
            @Parameter(description = "Branch ID header") @RequestHeader(value = "X-Branch-Id", required = false) String branchId,
            @Valid @RequestBody Map<String, Object> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.ok(reportService.executeReport(id,
                UUID.fromString((String)b.get("tenantId")), bid,
                (String)b.get("parameters"),
                b.get("requestedBy") != null ? UUID.fromString((String)b.get("requestedBy")) : null));
    }
    
    @GetMapping("/executions/{id}")
    @Operation(summary = "Get execution by ID", description = "Retrieves a specific report execution by its unique identifier")
    public ResponseEntity<ReportExecution> getExecution(
            @Parameter(description = "Report execution ID") @PathVariable UUID id,
            @Parameter(description = "Optional branch ID for filtering") @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        return reportService.getExecutionById(id)
                .filter(e -> branchId == null || branchId.isBlank() || e.getBranchId() == null || e.getBranchId().toString().equals(branchId))
                .map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/executions/by-report/{reportId}")
    @Operation(summary = "Get executions by report ID", description = "Retrieves all executions for a specific report definition")
    public ResponseEntity<List<ReportExecution>> getExecutionsByReport(
            @Parameter(description = "Report definition ID") @PathVariable UUID reportId,
            @Parameter(description = "Optional branch ID filter") @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(reportService.getExecutionsByReportIdAndBranch(reportId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(reportService.getExecutionsByReportId(reportId));
    }
}

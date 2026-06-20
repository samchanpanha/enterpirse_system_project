package com.reportsystem.reporting.domain.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.reportsystem.reporting.domain.model.*;
import com.reportsystem.reporting.domain.port.inbound.*;
import com.reportsystem.reporting.domain.port.outbound.*;
import java.time.Instant;
import java.util.*;
import java.util.UUID;


public class ReportServiceImpl implements ReportService {
    private final ReportDefinitionRepository defRepo;
    private final ReportExecutionRepository execRepo;
    private final AggregatedSnapshotRepository snapshotRepo;
    private final ObjectMapper objectMapper;

    public ReportServiceImpl(ReportDefinitionRepository defRepo, ReportExecutionRepository execRepo, AggregatedSnapshotRepository snapshotRepo, ObjectMapper objectMapper) {
        this.defRepo = defRepo; this.execRepo = execRepo; this.snapshotRepo = snapshotRepo; this.objectMapper = objectMapper;
    }

    @Override public ReportDefinition createDefinition(UUID tenantId, UUID branchId, String name, String type, String config, String layout) {
        return defRepo.save(ReportDefinition.builder().id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId).name(name).type(type).config(config != null ? config : "{}").layout(layout != null ? layout : "[]").system(false).createdAt(Instant.now()).build());
    }
    @Override public Optional<ReportDefinition> getDefinitionById(UUID id) { return defRepo.findById(id); }
    @Override public List<ReportDefinition> getDefinitionsByTenant(UUID tenantId) { return defRepo.findByTenantId(tenantId); }
    @Override public List<ReportDefinition> getDefinitionsByTenantAndBranch(UUID tenantId, UUID branchId) { return defRepo.findByTenantIdAndBranchId(tenantId, branchId); }

    @Override public ReportExecution executeReport(UUID reportId, UUID tenantId, UUID branchId, String parameters, UUID requestedBy) {
        ReportDefinition def = defRepo.findById(reportId).orElseThrow(() -> new IllegalArgumentException("Report not found: " + reportId));
        Instant startedAt = Instant.now();
        ReportExecution exec = ReportExecution.builder().id(UUID.randomUUID()).reportId(reportId).tenantId(tenantId).branchId(branchId != null ? branchId : def.getBranchId())
                .parameters(parameters != null ? parameters : "{}").status("running").startedAt(startedAt).requestedBy(requestedBy).createdAt(Instant.now()).build();
        exec = execRepo.save(exec);
        try {
            List<Map<String, Object>> rows = runEngine(def, tenantId, branchId);
            String resultJson = objectMapper.writeValueAsString(rows);
            Instant completedAt = Instant.now();
            int durationMs = (int) (completedAt.toEpochMilli() - startedAt.toEpochMilli());
            exec = ReportExecution.builder().id(exec.getId()).reportId(reportId).tenantId(tenantId).branchId(exec.getBranchId()).parameters(exec.getParameters())
                    .status("completed").outputUrl("/reports/output/" + exec.getId() + ".json").rowCount(rows.size()).durationMs(durationMs)
                    .resultData(resultJson).requestedBy(requestedBy).startedAt(startedAt).completedAt(completedAt).createdAt(exec.getCreatedAt()).build();
        } catch (Exception e) {
            Instant completedAt = Instant.now();
            int durationMs = (int) (completedAt.toEpochMilli() - startedAt.toEpochMilli());
            exec = ReportExecution.builder().id(exec.getId()).reportId(reportId).tenantId(tenantId).branchId(exec.getBranchId()).parameters(exec.getParameters())
                    .status("failed").rowCount(0).durationMs(durationMs).errorMessage(e.getMessage())
                    .requestedBy(requestedBy).startedAt(startedAt).completedAt(completedAt).createdAt(exec.getCreatedAt()).build();
        }
        return execRepo.save(exec);
    }

    @Override public Optional<ReportExecution> getExecutionById(UUID id) { return execRepo.findById(id); }
    @Override public List<ReportExecution> getExecutionsByReportId(UUID reportId) { return execRepo.findByReportId(reportId); }
    @Override public List<ReportExecution> getExecutionsByReportIdAndBranch(UUID reportId, UUID branchId) { return execRepo.findByReportIdAndBranchId(reportId, branchId); }

    private List<Map<String, Object>> runEngine(ReportDefinition def, UUID tenantId, UUID branchId) throws Exception {
        JsonNode configNode = objectMapper.readTree(def.getConfig() != null ? def.getConfig() : "{}");
        String source = configNode.hasNonNull("source") ? configNode.get("source").asText("snapshots") : "snapshots";
        switch (source) {
            case "snapshots":
                List<AggregatedSnapshot> snapshots = branchId != null ? snapshotRepo.findByTenantIdAndBranchId(tenantId, branchId) : snapshotRepo.findByTenantId(tenantId);
                List<Map<String, Object>> snapshotRows = new ArrayList<>();
                for (AggregatedSnapshot s : snapshots) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    row.put("snapshotType", s.getSnapshotType());
                    row.put("snapshotDate", s.getSnapshotDate() != null ? s.getSnapshotDate().toString() : null);
                    row.put("metrics", parseJson(s.getData()));
                    snapshotRows.add(row);
                }
                return snapshotRows;
            case "accounts":
                return List.of(
                        Map.of("accountCode", "1000-CASH", "accountName", "Cash on Hand", "balance", 15420.50),
                        Map.of("accountCode", "1200-AR", "accountName", "Accounts Receivable", "balance", 8750.00),
                        Map.of("accountCode", "2000-AP", "accountName", "Accounts Payable", "balance", -12300.00),
                        Map.of("accountCode", "3000-EQ", "accountName", "Owner Equity", "balance", 25000.00)
                );
            case "invoices":
                return List.of(
                        Map.of("invoiceNumber", "INV-001", "customer", "Acme Corp", "amount", 1200.00, "status", "PAID"),
                        Map.of("invoiceNumber", "INV-002", "customer", "Globex", "amount", 3450.00, "status", "OPEN"),
                        Map.of("invoiceNumber", "INV-003", "customer", "Soylent", "amount", 890.00, "status", "OVERDUE")
                );
            case "payments":
                return List.of(
                        Map.of("transactionId", "TXN-1001", "gateway", "ABA PayWay", "amount", 1200.00, "status", "SETTLED"),
                        Map.of("transactionId", "TXN-1002", "gateway", "Wing", "amount", 500.00, "status", "SETTLED"),
                        Map.of("transactionId", "TXN-1003", "gateway", "Cash", "amount", 150.00, "status", "PENDING"),
                        Map.of("transactionId", "TXN-1004", "gateway", "Pi Pay", "amount", 75.50, "status", "FAILED")
                );
            case "inventory":
                return List.of(
                        Map.of("sku", "SKU-001", "productName", "Rice 25kg", "quantity", 45, "unitCost", 18.50),
                        Map.of("sku", "SKU-002", "productName", "Cooking Oil 1L", "quantity", 120, "unitCost", 2.80),
                        Map.of("sku", "SKU-003", "productName", "Sugar 1kg", "quantity", 78, "unitCost", 1.20),
                        Map.of("sku", "SKU-004", "productName", "Instant Noodles", "quantity", 200, "unitCost", 0.45)
                );
            default:
                return new ArrayList<>();
        }
    }

    private Object parseJson(String json) {
        if (json == null || json.isBlank()) { return null; }
        try { return objectMapper.readValue(json, Object.class); }
        catch (Exception e) { return json; }
    }
}

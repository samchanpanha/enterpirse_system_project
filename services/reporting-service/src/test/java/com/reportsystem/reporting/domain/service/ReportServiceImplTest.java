package com.reportsystem.reporting.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reportsystem.reporting.domain.model.AggregatedSnapshot;
import com.reportsystem.reporting.domain.model.ReportDefinition;
import com.reportsystem.reporting.domain.model.ReportExecution;
import com.reportsystem.reporting.domain.port.outbound.AggregatedSnapshotRepository;
import com.reportsystem.reporting.domain.port.outbound.ReportDefinitionRepository;
import com.reportsystem.reporting.domain.port.outbound.ReportExecutionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportServiceImpl Unit Tests")
class ReportServiceImplTest {

    @Mock
    private ReportDefinitionRepository defRepo;

    @Mock
    private ReportExecutionRepository execRepo;

    @Mock
    private AggregatedSnapshotRepository snapshotRepo;

    private ReportServiceImpl reportService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        reportService = new ReportServiceImpl(defRepo, execRepo, snapshotRepo, objectMapper);
    }

    @Test
    @DisplayName("Should create report definition successfully")
    void testCreateDefinition() {
        // Given
        UUID tenantId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        String name = "Sales Report";
        String type = "sales";
        String config = "{\"source\": \"snapshots\"}";
        String layout = "[{\"field\": \"date\"}]";

        ReportDefinition savedDefinition = ReportDefinition.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .branchId(branchId)
                .name(name)
                .type(type)
                .config(config)
                .layout(layout)
                .system(false)
                .createdAt(Instant.now())
                .build();

        when(defRepo.save(any(ReportDefinition.class))).thenReturn(savedDefinition);

        // When
        ReportDefinition result = reportService.createDefinition(tenantId, branchId, name, type, config, layout);

        // Then
        assertNotNull(result);
        assertEquals(name, result.getName());
        assertEquals(type, result.getType());
        assertEquals(tenantId, result.getTenantId());
        assertEquals(branchId, result.getBranchId());
        verify(defRepo).save(any(ReportDefinition.class));
    }

    @Test
    @DisplayName("Should handle null config and layout with defaults")
    void testCreateDefinitionWithNullConfigAndLayout() {
        // Given
        UUID tenantId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        String name = "Test Report";
        String type = "test";

        ReportDefinition savedDefinition = ReportDefinition.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .branchId(branchId)
                .name(name)
                .type(type)
                .config("{}")
                .layout("[]")
                .system(false)
                .createdAt(Instant.now())
                .build();

        when(defRepo.save(any(ReportDefinition.class))).thenReturn(savedDefinition);

        // When
        ReportDefinition result = reportService.createDefinition(tenantId, branchId, name, type, null, null);

        // Then
        assertNotNull(result);
        assertEquals("{}", result.getConfig());
        assertEquals("[]", result.getLayout());
    }

    @Test
    @DisplayName("Should find definition by ID when exists")
    void testGetDefinitionById_Exists() {
        // Given
        UUID id = UUID.randomUUID();
        ReportDefinition definition = ReportDefinition.builder()
                .id(id)
                .tenantId(UUID.randomUUID())
                .name("Test Report")
                .type("test")
                .config("{}")
                .layout("[]")
                .system(false)
                .createdAt(Instant.now())
                .build();

        when(defRepo.findById(id)).thenReturn(Optional.of(definition));

        // When
        Optional<ReportDefinition> result = reportService.getDefinitionById(id);

        // Then
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals("Test Report", result.get().getName());
    }

    @Test
    @DisplayName("Should return empty when definition not found")
    void testGetDefinitionById_NotFound() {
        // Given
        UUID id = UUID.randomUUID();
        when(defRepo.findById(id)).thenReturn(Optional.empty());

        // When
        Optional<ReportDefinition> result = reportService.getDefinitionById(id);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should get definitions by tenant ID")
    void testGetDefinitionsByTenant() {
        // Given
        UUID tenantId = UUID.randomUUID();
        List<ReportDefinition> definitions = Arrays.asList(
                ReportDefinition.builder().id(UUID.randomUUID()).tenantId(tenantId).name("Report 1").type("type1").config("{}").layout("[]").system(false).createdAt(Instant.now()).build(),
                ReportDefinition.builder().id(UUID.randomUUID()).tenantId(tenantId).name("Report 2").type("type2").config("{}").layout("[]").system(false).createdAt(Instant.now()).build()
        );

        when(defRepo.findByTenantId(tenantId)).thenReturn(definitions);

        // When
        List<ReportDefinition> result = reportService.getDefinitionsByTenant(tenantId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(defRepo).findByTenantId(tenantId);
    }

    @Test
    @DisplayName("Should get definitions by tenant and branch ID")
    void testGetDefinitionsByTenantAndBranch() {
        // Given
        UUID tenantId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        List<ReportDefinition> definitions = Collections.singletonList(
                ReportDefinition.builder().id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId).name("Branch Report").type("type1").config("{}").layout("[]").system(false).createdAt(Instant.now()).build()
        );

        when(defRepo.findByTenantIdAndBranchId(tenantId, branchId)).thenReturn(definitions);

        // When
        List<ReportDefinition> result = reportService.getDefinitionsByTenantAndBranch(tenantId, branchId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(branchId, result.get(0).getBranchId());
    }

    @Test
    @DisplayName("Should execute report successfully with snapshots source")
    void testExecuteReport_Success() {
        // Given
        UUID reportId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID requestedBy = UUID.randomUUID();

        ReportDefinition definition = ReportDefinition.builder()
                .id(reportId)
                .tenantId(tenantId)
                .branchId(branchId)
                .name("Test Report")
                .type("test")
                .config("{\"source\": \"snapshots\"}")
                .layout("[]")
                .system(false)
                .createdAt(Instant.now())
                .build();

        List<AggregatedSnapshot> snapshots = Collections.singletonList(
                AggregatedSnapshot.builder()
                        .id(UUID.randomUUID())
                        .tenantId(tenantId)
                        .branchId(branchId)
                        .snapshotType("daily")
                        .snapshotDate(LocalDate.now())
                        .data("{\"revenue\": 1000}")
                        .createdAt(Instant.now())
                        .build()
        );

        ReportExecution runningExecution = ReportExecution.builder()
                .id(UUID.randomUUID())
                .reportId(reportId)
                .tenantId(tenantId)
                .branchId(branchId)
                .parameters("{}")
                .status("running")
                .startedAt(Instant.now())
                .requestedBy(requestedBy)
                .createdAt(Instant.now())
                .build();

        ReportExecution completedExecution = ReportExecution.builder()
                .id(runningExecution.getId())
                .reportId(reportId)
                .tenantId(tenantId)
                .branchId(branchId)
                .parameters("{}")
                .status("completed")
                .outputUrl("/reports/output/" + runningExecution.getId() + ".json")
                .rowCount(1)
                .durationMs(runningExecution.getDurationMs())
                .resultData("[{\"snapshotType\":\"daily\",\"snapshotDate\":\"" + LocalDate.now() + "\",\"metrics\":{\"revenue\":1000}}]")
                .requestedBy(requestedBy)
                .startedAt(runningExecution.getStartedAt())
                .completedAt(Instant.now())
                .createdAt(runningExecution.getCreatedAt())
                .build();

        when(defRepo.findById(reportId)).thenReturn(Optional.of(definition));
        when(snapshotRepo.findByTenantIdAndBranchId(tenantId, branchId)).thenReturn(snapshots);
        when(execRepo.save(any(ReportExecution.class))).thenReturn(runningExecution).thenReturn(completedExecution);

        // When
        ReportExecution result = reportService.executeReport(reportId, tenantId, branchId, null, requestedBy);

        // Then
        assertNotNull(result);
        assertEquals("completed", result.getStatus());
        assertEquals(1, result.getRowCount());
        verify(execRepo, times(2)).save(any(ReportExecution.class));
    }

    @Test
    @DisplayName("Should handle report execution failure")
    void testExecuteReport_Failure() {
        // Given
        UUID reportId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID requestedBy = UUID.randomUUID();

        ReportDefinition definition = ReportDefinition.builder()
                .id(reportId)
                .tenantId(tenantId)
                .branchId(branchId)
                .name("Test Report")
                .type("test")
                .config("{\"source\": \"invalid_source\"}")
                .layout("[]")
                .system(false)
                .createdAt(Instant.now())
                .build();

        ReportExecution runningExecution = ReportExecution.builder()
                .id(UUID.randomUUID())
                .reportId(reportId)
                .tenantId(tenantId)
                .branchId(branchId)
                .parameters("{}")
                .status("running")
                .startedAt(Instant.now())
                .requestedBy(requestedBy)
                .createdAt(Instant.now())
                .build();

        ReportExecution failedExecution = ReportExecution.builder()
                .id(runningExecution.getId())
                .reportId(reportId)
                .tenantId(tenantId)
                .branchId(branchId)
                .parameters("{}")
                .status("failed")
                .rowCount(0)
                .errorMessage(anyString())
                .requestedBy(requestedBy)
                .startedAt(runningExecution.getStartedAt())
                .completedAt(Instant.now())
                .createdAt(runningExecution.getCreatedAt())
                .build();

        when(defRepo.findById(reportId)).thenReturn(Optional.of(definition));
        when(execRepo.save(any(ReportExecution.class))).thenReturn(runningExecution).thenReturn(failedExecution);

        // When
        ReportExecution result = reportService.executeReport(reportId, tenantId, branchId, null, requestedBy);

        // Then
        assertNotNull(result);
        assertEquals("failed", result.getStatus());
        verify(execRepo, times(2)).save(any(ReportExecution.class));
    }

    @Test
    @DisplayName("Should throw exception when report definition not found")
    void testExecuteReport_DefinitionNotFound() {
        // Given
        UUID reportId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        when(defRepo.findById(reportId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            reportService.executeReport(reportId, tenantId, null, null, null)
        );
    }

    @Test
    @DisplayName("Should get execution by ID when exists")
    void testGetExecutionById_Exists() {
        // Given
        UUID id = UUID.randomUUID();
        ReportExecution execution = ReportExecution.builder()
                .id(id)
                .reportId(UUID.randomUUID())
                .tenantId(UUID.randomUUID())
                .branchId(UUID.randomUUID())
                .parameters("{}")
                .status("completed")
                .rowCount(10)
                .startedAt(Instant.now())
                .completedAt(Instant.now())
                .createdAt(Instant.now())
                .build();

        when(execRepo.findById(id)).thenReturn(Optional.of(execution));

        // When
        Optional<ReportExecution> result = reportService.getExecutionById(id);

        // Then
        assertTrue(result.isPresent());
        assertEquals(id, result.get().getId());
        assertEquals("completed", result.get().getStatus());
    }

    @Test
    @DisplayName("Should return empty when execution not found")
    void testGetExecutionById_NotFound() {
        // Given
        UUID id = UUID.randomUUID();
        when(execRepo.findById(id)).thenReturn(Optional.empty());

        // When
        Optional<ReportExecution> result = reportService.getExecutionById(id);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    @DisplayName("Should get executions by report ID")
    void testGetExecutionsByReportId() {
        // Given
        UUID reportId = UUID.randomUUID();
        List<ReportExecution> executions = Arrays.asList(
                ReportExecution.builder().id(UUID.randomUUID()).reportId(reportId).tenantId(UUID.randomUUID()).branchId(UUID.randomUUID()).parameters("{}").status("completed").rowCount(5).startedAt(Instant.now()).completedAt(Instant.now()).createdAt(Instant.now()).build(),
                ReportExecution.builder().id(UUID.randomUUID()).reportId(reportId).tenantId(UUID.randomUUID()).branchId(UUID.randomUUID()).parameters("{}").status("failed").rowCount(0).startedAt(Instant.now()).completedAt(Instant.now()).createdAt(Instant.now()).build()
        );

        when(execRepo.findByReportId(reportId)).thenReturn(executions);

        // When
        List<ReportExecution> result = reportService.getExecutionsByReportId(reportId);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(execRepo).findByReportId(reportId);
    }

    @Test
    @DisplayName("Should get executions by report ID and branch ID")
    void testGetExecutionsByReportIdAndBranch() {
        // Given
        UUID reportId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        List<ReportExecution> executions = Collections.singletonList(
                ReportExecution.builder().id(UUID.randomUUID()).reportId(reportId).tenantId(UUID.randomUUID()).branchId(branchId).parameters("{}").status("completed").rowCount(3).startedAt(Instant.now()).completedAt(Instant.now()).createdAt(Instant.now()).build()
        );

        when(execRepo.findByReportIdAndBranchId(reportId, branchId)).thenReturn(executions);

        // When
        List<ReportExecution> result = reportService.getExecutionsByReportIdAndBranch(reportId, branchId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(branchId, result.get(0).getBranchId());
    }

    @Test
    @DisplayName("Should execute report with accounts source")
    void testExecuteReport_AccountsSource() {
        // Given
        UUID reportId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID requestedBy = UUID.randomUUID();

        ReportDefinition definition = ReportDefinition.builder()
                .id(reportId)
                .tenantId(tenantId)
                .name("Accounts Report")
                .type("accounts")
                .config("{\"source\": \"accounts\"}")
                .layout("[]")
                .system(false)
                .createdAt(Instant.now())
                .build();

        ReportExecution runningExecution = ReportExecution.builder()
                .id(UUID.randomUUID())
                .reportId(reportId)
                .tenantId(tenantId)
                .branchId(null)
                .parameters("{}")
                .status("running")
                .startedAt(Instant.now())
                .requestedBy(requestedBy)
                .createdAt(Instant.now())
                .build();

        when(defRepo.findById(reportId)).thenReturn(Optional.of(definition));
        when(execRepo.save(any(ReportExecution.class))).thenReturn(runningExecution);

        // When
        ReportExecution result = reportService.executeReport(reportId, tenantId, null, null, requestedBy);

        // Then
        assertNotNull(result);
        assertEquals("completed", result.getStatus());
        assertEquals(4, result.getRowCount()); // 4 mock accounts
    }

    @Test
    @DisplayName("Should execute report with invoices source")
    void testExecuteReport_InvoicesSource() {
        // Given
        UUID reportId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID requestedBy = UUID.randomUUID();

        ReportDefinition definition = ReportDefinition.builder()
                .id(reportId)
                .tenantId(tenantId)
                .name("Invoices Report")
                .type("invoices")
                .config("{\"source\": \"invoices\"}")
                .layout("[]")
                .system(false)
                .createdAt(Instant.now())
                .build();

        ReportExecution runningExecution = ReportExecution.builder()
                .id(UUID.randomUUID())
                .reportId(reportId)
                .tenantId(tenantId)
                .branchId(null)
                .parameters("{}")
                .status("running")
                .startedAt(Instant.now())
                .requestedBy(requestedBy)
                .createdAt(Instant.now())
                .build();

        when(defRepo.findById(reportId)).thenReturn(Optional.of(definition));
        when(execRepo.save(any(ReportExecution.class))).thenReturn(runningExecution);

        // When
        ReportExecution result = reportService.executeReport(reportId, tenantId, null, null, requestedBy);

        // Then
        assertNotNull(result);
        assertEquals("completed", result.getStatus());
        assertEquals(3, result.getRowCount()); // 3 mock invoices
    }

    @Test
    @DisplayName("Should execute report with payments source")
    void testExecuteReport_PaymentsSource() {
        // Given
        UUID reportId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID requestedBy = UUID.randomUUID();

        ReportDefinition definition = ReportDefinition.builder()
                .id(reportId)
                .tenantId(tenantId)
                .name("Payments Report")
                .type("payments")
                .config("{\"source\": \"payments\"}")
                .layout("[]")
                .system(false)
                .createdAt(Instant.now())
                .build();

        ReportExecution runningExecution = ReportExecution.builder()
                .id(UUID.randomUUID())
                .reportId(reportId)
                .tenantId(tenantId)
                .branchId(null)
                .parameters("{}")
                .status("running")
                .startedAt(Instant.now())
                .requestedBy(requestedBy)
                .createdAt(Instant.now())
                .build();

        when(defRepo.findById(reportId)).thenReturn(Optional.of(definition));
        when(execRepo.save(any(ReportExecution.class))).thenReturn(runningExecution);

        // When
        ReportExecution result = reportService.executeReport(reportId, tenantId, null, null, requestedBy);

        // Then
        assertNotNull(result);
        assertEquals("completed", result.getStatus());
        assertEquals(4, result.getRowCount()); // 4 mock payments
    }

    @Test
    @DisplayName("Should execute report with inventory source")
    void testExecuteReport_InventorySource() {
        // Given
        UUID reportId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID requestedBy = UUID.randomUUID();

        ReportDefinition definition = ReportDefinition.builder()
                .id(reportId)
                .tenantId(tenantId)
                .name("Inventory Report")
                .type("inventory")
                .config("{\"source\": \"inventory\"}")
                .layout("[]")
                .system(false)
                .createdAt(Instant.now())
                .build();

        ReportExecution runningExecution = ReportExecution.builder()
                .id(UUID.randomUUID())
                .reportId(reportId)
                .tenantId(tenantId)
                .branchId(null)
                .parameters("{}")
                .status("running")
                .startedAt(Instant.now())
                .requestedBy(requestedBy)
                .createdAt(Instant.now())
                .build();

        when(defRepo.findById(reportId)).thenReturn(Optional.of(definition));
        when(execRepo.save(any(ReportExecution.class))).thenReturn(runningExecution);

        // When
        ReportExecution result = reportService.executeReport(reportId, tenantId, null, null, requestedBy);

        // Then
        assertNotNull(result);
        assertEquals("completed", result.getStatus());
        assertEquals(4, result.getRowCount()); // 4 mock inventory items
    }
}

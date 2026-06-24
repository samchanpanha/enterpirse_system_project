package com.reportsystem.reporting.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reportsystem.reporting.domain.model.ReportDefinition;
import com.reportsystem.reporting.domain.model.ReportExecution;
import com.reportsystem.reporting.domain.port.inbound.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Instant;
import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@DisplayName("ReportController Unit Tests")
class ReportControllerTest {

    private MockMvc mockMvc;
    private ReportService reportService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        reportService = mock(ReportService.class);
        ReportController controller = new ReportController(reportService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should create report definition successfully")
    void testCreateDefinition() throws Exception {
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

        when(reportService.createDefinition(any(), any(), any(), any(), any(), any())).thenReturn(savedDefinition);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("tenantId", tenantId.toString());
        requestBody.put("name", name);
        requestBody.put("type", type);
        requestBody.put("config", config);
        requestBody.put("layout", layout);

        // When & Then
        mockMvc.perform(post("/reports/definitions")
                .header("X-Branch-Id", branchId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(savedDefinition.getId().toString()))
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.type").value(type));

        verify(reportService).createDefinition(
                eq(tenantId), eq(branchId), eq(name), eq(type), eq(config), eq(layout)
        );
    }

    @Test
    @DisplayName("Should get report definition by ID")
    void testGetDefinitionById() throws Exception {
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

        when(reportService.getDefinitionById(id)).thenReturn(Optional.of(definition));

        // When & Then
        mockMvc.perform(get("/reports/definitions/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.name").value("Test Report"));
    }

    @Test
    @DisplayName("Should return 404 when definition not found")
    void testGetDefinitionById_NotFound() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        when(reportService.getDefinitionById(id)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/reports/definitions/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get definitions by tenant ID")
    void testGetDefinitionsByTenant() throws Exception {
        // Given
        UUID tenantId = UUID.randomUUID();
        List<ReportDefinition> definitions = Arrays.asList(
                ReportDefinition.builder().id(UUID.randomUUID()).tenantId(tenantId).name("Report 1").type("type1").config("{}").layout("[]").system(false).createdAt(Instant.now()).build(),
                ReportDefinition.builder().id(UUID.randomUUID()).tenantId(tenantId).name("Report 2").type("type2").config("{}").layout("[]").system(false).createdAt(Instant.now()).build()
        );

        when(reportService.getDefinitionsByTenant(tenantId)).thenReturn(definitions);

        // When & Then
        mockMvc.perform(get("/reports/definitions/by-tenant/{tenantId}", tenantId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Should get definitions by tenant and branch ID")
    void testGetDefinitionsByTenantAndBranch() throws Exception {
        // Given
        UUID tenantId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        List<ReportDefinition> definitions = Collections.singletonList(
                ReportDefinition.builder().id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId).name("Branch Report").type("type1").config("{}").layout("[]").system(false).createdAt(Instant.now()).build()
        );

        when(reportService.getDefinitionsByTenantAndBranch(tenantId, branchId)).thenReturn(definitions);

        // When & Then
        mockMvc.perform(get("/reports/definitions/by-tenant/{tenantId}", tenantId)
                .header("X-Branch-Id", branchId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    @DisplayName("Should execute report successfully")
    void testExecuteReport() throws Exception {
        // Given
        UUID reportId = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        UUID requestedBy = UUID.randomUUID();

        ReportExecution execution = ReportExecution.builder()
                .id(UUID.randomUUID())
                .reportId(reportId)
                .tenantId(tenantId)
                .branchId(branchId)
                .parameters("{}")
                .status("completed")
                .rowCount(10)
                .startedAt(Instant.now())
                .completedAt(Instant.now())
                .createdAt(Instant.now())
                .requestedBy(requestedBy)
                .build();

        when(reportService.executeReport(any(), any(), any(), any(), any())).thenReturn(execution);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("tenantId", tenantId.toString());
        requestBody.put("requestedBy", requestedBy.toString());

        // When & Then
        mockMvc.perform(post("/reports/definitions/{id}/execute", reportId)
                .header("X-Branch-Id", branchId.toString())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(execution.getId().toString()))
                .andExpect(jsonPath("$.status").value("completed"))
                .andExpect(jsonPath("$.rowCount").value(10));
    }

    @Test
    @DisplayName("Should get execution by ID")
    void testGetExecutionById() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        ReportExecution execution = ReportExecution.builder()
                .id(id)
                .reportId(UUID.randomUUID())
                .tenantId(UUID.randomUUID())
                .branchId(UUID.randomUUID())
                .parameters("{}")
                .status("completed")
                .rowCount(5)
                .startedAt(Instant.now())
                .completedAt(Instant.now())
                .createdAt(Instant.now())
                .build();

        when(reportService.getExecutionById(id)).thenReturn(Optional.of(execution));

        // When & Then
        mockMvc.perform(get("/reports/executions/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.status").value("completed"));
    }

    @Test
    @DisplayName("Should return 404 when execution not found")
    void testGetExecutionById_NotFound() throws Exception {
        // Given
        UUID id = UUID.randomUUID();
        when(reportService.getExecutionById(id)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/reports/executions/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Should get executions by report ID")
    void testGetExecutionsByReportId() throws Exception {
        // Given
        UUID reportId = UUID.randomUUID();
        List<ReportExecution> executions = Arrays.asList(
                ReportExecution.builder().id(UUID.randomUUID()).reportId(reportId).tenantId(UUID.randomUUID()).branchId(UUID.randomUUID()).parameters("{}").status("completed").rowCount(5).startedAt(Instant.now()).completedAt(Instant.now()).createdAt(Instant.now()).build(),
                ReportExecution.builder().id(UUID.randomUUID()).reportId(reportId).tenantId(UUID.randomUUID()).branchId(UUID.randomUUID()).parameters("{}").status("failed").rowCount(0).startedAt(Instant.now()).completedAt(Instant.now()).createdAt(Instant.now()).build()
        );

        when(reportService.getExecutionsByReportId(reportId)).thenReturn(executions);

        // When & Then
        mockMvc.perform(get("/reports/executions/by-report/{reportId}", reportId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Should get executions by report ID and branch ID")
    void testGetExecutionsByReportIdAndBranch() throws Exception {
        // Given
        UUID reportId = UUID.randomUUID();
        UUID branchId = UUID.randomUUID();
        List<ReportExecution> executions = Collections.singletonList(
                ReportExecution.builder().id(UUID.randomUUID()).reportId(reportId).tenantId(UUID.randomUUID()).branchId(branchId).parameters("{}").status("completed").rowCount(3).startedAt(Instant.now()).completedAt(Instant.now()).createdAt(Instant.now()).build()
        );

        when(reportService.getExecutionsByReportIdAndBranch(reportId, branchId)).thenReturn(executions);

        // When & Then
        mockMvc.perform(get("/reports/executions/by-report/{reportId}", reportId)
                .header("X-Branch-Id", branchId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));
    }
}

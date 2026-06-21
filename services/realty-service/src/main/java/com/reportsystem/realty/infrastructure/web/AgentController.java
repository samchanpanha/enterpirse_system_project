package com.reportsystem.realty.infrastructure.web;

import com.reportsystem.realty.domain.model.Agent;
import com.reportsystem.realty.domain.port.inbound.AgentService;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agents")
public class AgentController {

    private final AgentService agentService;
    public AgentController(AgentService agentService) { this.agentService = agentService; }

    @PostMapping
    public ResponseEntity<Agent> create(@RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                        @RequestBody Map<String, String> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(
                agentService.createAgent(UUID.fromString(b.get("tenantId")), bid, b.get("name"), b.get("email")));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Agent> get(@PathVariable UUID id) {
        return agentService.getAgentById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-tenant/{tenantId}")
    public ResponseEntity<List<Agent>> getByTenant(@PathVariable UUID tenantId,
                                                   @RequestHeader(value = "X-Branch-Id", required = false) String branchId) {
        if (branchId != null && !branchId.isBlank()) {
            return ResponseEntity.ok(agentService.getAgentsByTenantAndBranch(tenantId, UUID.fromString(branchId)));
        }
        return ResponseEntity.ok(agentService.getAgentsByTenant(tenantId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Agent> update(@PathVariable UUID id, @RequestBody Agent agent) {
        return agentService.getAgentById(id).map(existing -> {
            Agent updated = Agent.builder()
                    .id(existing.getId()).tenantId(existing.getTenantId()).branchId(existing.getBranchId())
                    .userId(agent.getUserId()).name(agent.getName()).nameKh(agent.getNameKh())
                    .phone(agent.getPhone()).email(agent.getEmail())
                    .licenseNumber(agent.getLicenseNumber()).bio(agent.getBio())
                    .avatarUrl(agent.getAvatarUrl()).rating(agent.getRating())
                    .totalSales(agent.getTotalSales()).totalListings(agent.getTotalListings())
                    .active(agent.isActive())
                    .createdAt(existing.getCreatedAt()).updatedAt(java.time.Instant.now())
                    .build();
            return ResponseEntity.ok(agentService.updateAgent(updated));
        }).orElse(ResponseEntity.notFound().build());
    }
}

package com.reportsystem.property.domain.service;

import com.reportsystem.property.domain.model.MaintenanceTicket;
import com.reportsystem.property.domain.port.inbound.MaintenanceUseCase;
import com.reportsystem.property.domain.port.outbound.MaintenanceRepository;
import com.reportsystem.property.domain.port.outbound.UnitRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

public class MaintenanceService implements MaintenanceUseCase {

    private final MaintenanceRepository maintenanceRepository;
    private final UnitRepository unitRepository;

    public MaintenanceService(MaintenanceRepository maintenanceRepository, UnitRepository unitRepository) {
        this.maintenanceRepository = maintenanceRepository;
        this.unitRepository = unitRepository;
    }

    @Override
    public MaintenanceTicket createTicket(UUID tenantId, UUID branchId, UUID unitId, String title, String description, String priority, String category) {
        MaintenanceTicket ticket = MaintenanceTicket.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId).unitId(unitId)
                .title(title).description(description)
                .priority(priority != null ? priority : "medium")
                .category(category).status("open")
                .createdAt(Instant.now())
                .build();
        return maintenanceRepository.save(ticket);
    }

    @Override
    public Optional<MaintenanceTicket> getTicketById(UUID id) {
        return maintenanceRepository.findById(id);
    }

    @Override
    public List<MaintenanceTicket> getTicketsByUnit(UUID unitId) {
        return maintenanceRepository.findByUnitId(unitId);
    }

    @Override
    public List<MaintenanceTicket> getTicketsByProperty(UUID propertyId) {
        List<UUID> unitIds = unitRepository.findByPropertyId(propertyId).stream()
            .map(u -> u.getId())
            .collect(Collectors.toList());
        if (unitIds.isEmpty()) return List.of();
        return maintenanceRepository.findByUnitIds(unitIds);
    }

    @Override
    public List<MaintenanceTicket> getTicketsByTenant(UUID tenantId) {
        return maintenanceRepository.findByTenantId(tenantId);
    }

    @Override
    public List<MaintenanceTicket> getTicketsByTenantAndBranch(UUID tenantId, UUID branchId) {
        return maintenanceRepository.findByTenantIdAndBranchId(tenantId, branchId);
    }

    @Override
    public MaintenanceTicket updateStatus(UUID id, String status) {
        MaintenanceTicket existing = maintenanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));
        MaintenanceTicket updated = MaintenanceTicket.builder()
                .id(existing.getId()).tenantId(existing.getTenantId()).unitId(existing.getUnitId())
                .reportedBy(existing.getReportedBy()).title(existing.getTitle())
                .description(existing.getDescription()).priority(existing.getPriority())
                .category(existing.getCategory()).status(status)
                .assignedTo(existing.getAssignedTo())
                .costEstimate(existing.getCostEstimate()).actualCost(existing.getActualCost())
                .completedAt(existing.getCompletedAt())
                .createdAt(existing.getCreatedAt()).updatedAt(Instant.now())
                .build();
        return maintenanceRepository.save(updated);
    }

    @Override
    public MaintenanceTicket assignTicket(UUID id, String assignedTo) {
        MaintenanceTicket existing = maintenanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));
        MaintenanceTicket updated = MaintenanceTicket.builder()
                .id(existing.getId()).tenantId(existing.getTenantId()).unitId(existing.getUnitId())
                .reportedBy(existing.getReportedBy()).title(existing.getTitle())
                .description(existing.getDescription()).priority(existing.getPriority())
                .category(existing.getCategory()).status("assigned")
                .assignedTo(assignedTo)
                .costEstimate(existing.getCostEstimate()).actualCost(existing.getActualCost())
                .completedAt(existing.getCompletedAt())
                .createdAt(existing.getCreatedAt()).updatedAt(Instant.now())
                .build();
        return maintenanceRepository.save(updated);
    }

    @Override
    public MaintenanceTicket completeTicket(UUID id, BigDecimal actualCost) {
        MaintenanceTicket existing = maintenanceRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found: " + id));
        MaintenanceTicket updated = MaintenanceTicket.builder()
                .id(existing.getId()).tenantId(existing.getTenantId()).unitId(existing.getUnitId())
                .reportedBy(existing.getReportedBy()).title(existing.getTitle())
                .description(existing.getDescription()).priority(existing.getPriority())
                .category(existing.getCategory()).status("completed")
                .assignedTo(existing.getAssignedTo())
                .costEstimate(existing.getCostEstimate())
                .actualCost(actualCost)
                .completedAt(Instant.now())
                .createdAt(existing.getCreatedAt()).updatedAt(Instant.now())
                .build();
        return maintenanceRepository.save(updated);
    }
}

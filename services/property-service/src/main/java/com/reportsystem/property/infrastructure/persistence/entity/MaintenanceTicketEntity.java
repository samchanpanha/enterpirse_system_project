package com.reportsystem.property.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "maintenance_tickets")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class MaintenanceTicketEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "unit_id", nullable = false)
    private UUID unitId;
    @Column(name = "reported_by")
    private String reportedBy;
    @Column(nullable = false)
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String priority;
    private String category;
    private String status;
    @Column(name = "assigned_to")
    private String assignedTo;
    @Column(name = "cost_estimate")
    private BigDecimal costEstimate;
    @Column(name = "actual_cost")
    private BigDecimal actualCost;
    @Column(name = "completed_at")
    private Instant completedAt;
    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
}

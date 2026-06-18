package com.reportsystem.property.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MaintenanceTicket {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private final UUID unitId;
    private String reportedBy;
    private String title;
    private String description;
    private String priority;
    private String category;
    private String status;
    private String assignedTo;
    private BigDecimal costEstimate;
    private BigDecimal actualCost;
    private Instant completedAt;
    private final Instant createdAt;
    private Instant updatedAt;
}

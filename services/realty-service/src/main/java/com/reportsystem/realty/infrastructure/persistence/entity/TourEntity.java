package com.reportsystem.realty.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity @Table(name = "tours")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class TourEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "listing_id")
    private UUID listingId;
    @Column(name = "agent_id")
    private UUID agentId;
    @Column(name = "lead_id")
    private UUID leadId;
    @Column(name = "scheduled_at", nullable = false)
    private Instant scheduledAt;
    private String status;
    @Column(columnDefinition = "TEXT")
    private String notes;
    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
}

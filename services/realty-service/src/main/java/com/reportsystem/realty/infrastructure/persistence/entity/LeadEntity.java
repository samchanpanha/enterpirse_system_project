package com.reportsystem.realty.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity @Table(name = "leads")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class LeadEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "listing_id")
    private UUID listingId;
    @Column(name = "agent_id")
    private UUID agentId;
    @Column(nullable = false)
    private String name;
    private String phone;
    private String email;
    @Column(columnDefinition = "TEXT")
    private String message;
    private String source;
    private String status;
    @Column(name = "budget_min", precision = 15, scale = 2)
    private BigDecimal budgetMin;
    @Column(name = "budget_max", precision = 15, scale = 2)
    private BigDecimal budgetMax;
    @Column(name = "property_type_preference")
    private String propertyTypePreference;
    @Column(name = "preferred_district")
    private String preferredDistrict;
    @Column(columnDefinition = "TEXT")
    private String notes;
    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
}

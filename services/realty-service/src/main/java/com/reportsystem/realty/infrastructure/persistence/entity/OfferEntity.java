package com.reportsystem.realty.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Entity @Table(name = "offers")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class OfferEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "listing_id", nullable = false)
    private UUID listingId;
    @Column(name = "buyer_id")
    private UUID buyerId;
    @Column(name = "agent_id")
    private UUID agentId;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    private String currency;
    @Column(columnDefinition = "TEXT")
    private String terms;
    private String status;
    @Column(name = "expiry_date")
    private LocalDate expiryDate;
    @Column(name = "counter_amount", precision = 15, scale = 2)
    private BigDecimal counterAmount;
    @Column(name = "rejected_reason", columnDefinition = "TEXT")
    private String rejectedReason;
    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
}

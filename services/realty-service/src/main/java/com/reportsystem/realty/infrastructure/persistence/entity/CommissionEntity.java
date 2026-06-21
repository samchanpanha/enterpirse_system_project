package com.reportsystem.realty.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity @Table(name = "commissions")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class CommissionEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "offer_id", nullable = false)
    private UUID offerId;
    @Column(name = "agent_id")
    private UUID agentId;
    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    @Column(precision = 5, scale = 2)
    private BigDecimal percentage;
    private String status;
    @Column(name = "paid_at")
    private Instant paidAt;
    @Column(name = "branch_id", nullable = false)
    private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}

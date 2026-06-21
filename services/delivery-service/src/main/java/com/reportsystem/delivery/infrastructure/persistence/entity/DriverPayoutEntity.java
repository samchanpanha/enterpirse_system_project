package com.reportsystem.delivery.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;

@Entity @Table(name = "driver_payouts")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class DriverPayoutEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;
    @Column(name = "driver_id", nullable = false)
    private UUID driverId;
    @Column(name = "period_start", nullable = false)
    private LocalDate periodStart;
    @Column(name = "period_end", nullable = false)
    private LocalDate periodEnd;
    @Column(name = "total_deliveries")
    private Integer totalDeliveries;
    @Column(name = "total_distance")
    private BigDecimal totalDistance;
    @Column(name = "total_earnings")
    private BigDecimal totalEarnings;
    @Column(name = "commission_amount")
    private BigDecimal commissionAmount;
    @Column(name = "bonus_amount")
    private BigDecimal bonusAmount;
    @Column(name = "deduction_amount")
    private BigDecimal deductionAmount;
    @Column(name = "net_amount")
    private BigDecimal netAmount;
    private String status;
    @Column(name = "paid_at")
    private Instant paidAt;
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
    @Column(name = "updated_at")
    private Instant updatedAt;
}

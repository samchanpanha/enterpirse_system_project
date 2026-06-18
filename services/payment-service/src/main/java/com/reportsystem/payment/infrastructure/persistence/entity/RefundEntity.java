package com.reportsystem.payment.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import lombok.*;


@Entity
@Table(name = "refunds")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RefundEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    @Column(name = "transaction_id", nullable = false) private UUID transactionId;
    private BigDecimal amount; private String reason;
    @Column(name = "gateway_ref") private String gatewayRef;
    private String status; @Column(name = "processed_at") private Instant processedAt;
    @Column(name = "created_by") private UUID createdBy;
    @Column(name = "branch_id", nullable = false) private UUID branchId;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
}
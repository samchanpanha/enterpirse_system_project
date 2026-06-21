package com.reportsystem.payment.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "payment_gateway_configs")
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class PaymentGatewayConfigEntity {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(name = "tenant_id", nullable = false) private UUID tenantId;
    private String gateway;
    @Column(columnDefinition = "JSONB DEFAULT '{}'") private String config;
    @Column(name = "is_active") private boolean active;
    @Column(name = "is_sandbox") private boolean sandbox;
    @Column(name = "created_at", nullable = false, updatable = false) private Instant createdAt;
    @Column(name = "updated_at") private Instant updatedAt;
}

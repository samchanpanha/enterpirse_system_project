package com.reportsystem.payment.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class PaymentGatewayConfig {
    private final UUID id;
    private final UUID tenantId;
    private String gateway;
    private String config;
    private boolean active;
    private boolean sandbox;
    private final Instant createdAt;
    private Instant updatedAt;
}

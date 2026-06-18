package com.reportsystem.payment.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class Refund {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private UUID transactionId;
    private BigDecimal amount;
    private String reason;
    private String gatewayRef;
    private String status;
    private Instant processedAt;
    private UUID createdBy;
    private final Instant createdAt;
}
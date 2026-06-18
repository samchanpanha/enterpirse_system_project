package com.reportsystem.payment.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class Transaction {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private String transactionId;
    private UUID invoiceId;
    private BigDecimal amount;
    private String currency;
    private String gateway;
    private String gatewayRef;
    private String method;
    private String status;
    private String customerName;
    private String customerPhone;
    private String sourceType;
    private UUID sourceId;
    private String metadata;
    private Instant paidAt;
    private final Instant createdAt;
    private Instant updatedAt;
}
package com.reportsystem.payment.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class GatewayLog {
    private final UUID id;
    private UUID transactionId;
    private String gateway;
    private String requestBody;
    private String responseBody;
    private Integer httpStatus;
    private Integer durationMs;
    private Boolean success;
    private String errorMessage;
    private final Instant createdAt;
}
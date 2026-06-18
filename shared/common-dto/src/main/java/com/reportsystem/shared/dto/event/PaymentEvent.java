package com.reportsystem.shared.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentEvent(
    @JsonProperty("paymentId") @NotNull UUID paymentId,
    @JsonProperty("tenantId") @NotNull UUID tenantId,
    @JsonProperty("invoiceId") UUID invoiceId,
    @JsonProperty("amount") @Positive BigDecimal amount,
    @JsonProperty("gateway") @NotBlank String gateway,
    @JsonProperty("status") @NotBlank String status,
    @JsonProperty("paidAt") Instant paidAt
) {}

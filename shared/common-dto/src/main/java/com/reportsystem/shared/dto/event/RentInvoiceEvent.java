package com.reportsystem.shared.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record RentInvoiceEvent(
    @JsonProperty("invoiceId") @NotNull UUID invoiceId,
    @JsonProperty("tenantId") @NotNull UUID tenantId,
    @JsonProperty("unitId") @NotNull UUID unitId,
    @JsonProperty("leaseId") @NotNull UUID leaseId,
    @JsonProperty("tenantName") @NotBlank String tenantName,
    @JsonProperty("amount") @Positive BigDecimal amount,
    @JsonProperty("dueDate") @NotNull LocalDate dueDate,
    @JsonProperty("periodStart") @NotNull LocalDate periodStart,
    @JsonProperty("periodEnd") @NotNull LocalDate periodEnd
) {}

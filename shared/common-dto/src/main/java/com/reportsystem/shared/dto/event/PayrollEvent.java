package com.reportsystem.shared.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record PayrollEvent(
    @JsonProperty("payrollPeriodId") @NotNull UUID payrollPeriodId,
    @JsonProperty("tenantId") @NotNull UUID tenantId,
    @JsonProperty("periodStart") @NotNull LocalDate periodStart,
    @JsonProperty("periodEnd") @NotNull LocalDate periodEnd,
    @JsonProperty("totalGross") @Positive BigDecimal totalGross,
    @JsonProperty("totalTax") BigDecimal totalTax,
    @JsonProperty("totalNSSF") BigDecimal totalNSSF,
    @JsonProperty("totalNet") @Positive BigDecimal totalNet,
    @JsonProperty("employeeCount") int employeeCount
) {}

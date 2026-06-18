package com.reportsystem.shared.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record StockEvent(
    @JsonProperty("eventType") @NotBlank String eventType,
    @JsonProperty("tenantId") @NotNull UUID tenantId,
    @JsonProperty("poId") UUID poId,
    @JsonProperty("receivedDate") LocalDate receivedDate,
    @JsonProperty("items") @NotNull List<StockItem> items
) {
    public record StockItem(
        @JsonProperty("productId") @NotNull UUID productId,
        @JsonProperty("productName") @NotBlank String productName,
        @JsonProperty("quantity") @Positive BigDecimal quantity,
        @JsonProperty("unitCost") @Positive BigDecimal unitCost,
        @JsonProperty("totalCost") @Positive BigDecimal totalCost
    ) {}
}

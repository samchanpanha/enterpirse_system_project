package com.reportsystem.shared.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record SaleEvent(
    @JsonProperty("saleId") @NotNull UUID saleId,
    @JsonProperty("tenantId") @NotNull UUID tenantId,
    @JsonProperty("outletId") @NotNull UUID outletId,
    @JsonProperty("orderNumber") @NotBlank String orderNumber,
    @JsonProperty("items") @NotNull List<SaleItem> items,
    @JsonProperty("subtotal") @Positive BigDecimal subtotal,
    @JsonProperty("discount") BigDecimal discount,
    @JsonProperty("taxAmount") @Positive BigDecimal taxAmount,
    @JsonProperty("total") @Positive BigDecimal total,
    @JsonProperty("paymentMethod") @NotBlank String paymentMethod,
    @JsonProperty("soldAt") @NotNull Instant soldAt
) {
    public record SaleItem(
        @JsonProperty("menuItemId") @NotNull UUID menuItemId,
        @JsonProperty("name") @NotBlank String name,
        @JsonProperty("quantity") @Positive int quantity,
        @JsonProperty("unitPrice") @Positive BigDecimal unitPrice,
        @JsonProperty("subtotal") @Positive BigDecimal subtotal
    ) {}
}

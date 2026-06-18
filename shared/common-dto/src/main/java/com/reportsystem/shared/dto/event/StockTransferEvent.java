package com.reportsystem.shared.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * Event published when a stock transfer changes state.
 * Consumed by:
 *   - finance-service: posts inter-branch journal entries on transfer.received
 *   - reporting-service: audit trail for cross-branch ops
 */
public record StockTransferEvent(
    @JsonProperty("eventType") @NotBlank String eventType,
    @JsonProperty("transferId") @NotNull UUID transferId,
    @JsonProperty("transferNumber") @NotBlank String transferNumber,
    @JsonProperty("tenantId") @NotNull UUID tenantId,
    @JsonProperty("fromBranchId") @NotNull UUID fromBranchId,
    @JsonProperty("toBranchId") @NotNull UUID toBranchId,
    @JsonProperty("status") @NotBlank String status,
    @JsonProperty("totalValue") BigDecimal totalValue,
    @JsonProperty("items") @NotNull List<TransferItem> items,
    @JsonProperty("occurredAt") @NotNull Instant occurredAt
) {
    public record TransferItem(
        @JsonProperty("productId") @NotNull UUID productId,
        @JsonProperty("productName") String productName,
        @JsonProperty("quantity") @NotNull BigDecimal quantity,
        @JsonProperty("receivedQuantity") BigDecimal receivedQuantity,
        @JsonProperty("unitCost") BigDecimal unitCost
    ) {}

    public static final String TYPE_REQUESTED = "stock.transfer.requested";
    public static final String TYPE_SHIPPED = "stock.transfer.shipped";
    public static final String TYPE_RECEIVED = "stock.transfer.received";
    public static final String TYPE_CANCELLED = "stock.transfer.cancelled";
}

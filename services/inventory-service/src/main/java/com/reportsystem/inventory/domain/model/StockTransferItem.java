package com.reportsystem.inventory.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockTransferItem {
    private UUID id;
    private UUID transferId;
    private UUID productId;
    private String productName;        // populated from Product for display
    private BigDecimal quantity;
    private BigDecimal receivedQuantity;
    private BigDecimal unitCost;
    private String discrepancyNotes;
    private int lineOrder;
    private Instant createdAt;
}

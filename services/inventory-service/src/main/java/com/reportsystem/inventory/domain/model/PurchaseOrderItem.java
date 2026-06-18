package com.reportsystem.inventory.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter @Builder @AllArgsConstructor
public class PurchaseOrderItem {
    private final UUID id;
    private final UUID purchaseOrderId;
    private UUID productId;
    private BigDecimal quantityOrdered;
    private BigDecimal quantityReceived;
    private BigDecimal unitCost;
    private BigDecimal subtotal;
    private final Instant createdAt;
}

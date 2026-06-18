package com.reportsystem.inventory.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter @Builder @AllArgsConstructor
public class Product {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private UUID categoryId;
    private String name;
    private String nameKh;
    private String sku;
    private String barcode;
    private String unit;
    private BigDecimal unitPrice;
    private BigDecimal costPrice;
    private BigDecimal minStock;
    private BigDecimal maxStock;
    private boolean tracked;
    private boolean active;
    private String imageUrl;
    private final Instant createdAt;
    private Instant updatedAt;
}

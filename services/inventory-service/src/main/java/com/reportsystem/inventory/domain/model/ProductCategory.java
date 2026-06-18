package com.reportsystem.inventory.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter @Builder @AllArgsConstructor
public class ProductCategory {
    private final UUID id;
    private final UUID tenantId;
    private String name;
    private UUID parentId;
    private final Instant createdAt;
}

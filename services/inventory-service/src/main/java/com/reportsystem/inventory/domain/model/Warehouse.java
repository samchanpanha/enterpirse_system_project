package com.reportsystem.inventory.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter @Builder @AllArgsConstructor
public class Warehouse {
    private final UUID id;
    private final UUID tenantId;
    private String name;
    private String type;
    private String location;
    private boolean active;
    private final Instant createdAt;
}

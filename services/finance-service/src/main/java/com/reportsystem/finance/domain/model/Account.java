package com.reportsystem.finance.domain.model;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.*;

@Getter
@Builder
@AllArgsConstructor
public class Account {
    private final UUID id;
    private final UUID tenantId;
    private final UUID branchId;
    private String code;
    private String name;
    private String type;
    private boolean active;
    private boolean contra;
    private UUID parentId;
    private final Instant createdAt;
    private Instant updatedAt;
}
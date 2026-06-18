package com.reportsystem.auth.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Feature — a unit of functionality that can be enabled/disabled per client (tenant).
 *
 * Features are organised in a tree by `parentCode`. Each feature has a tier
 * requirement (BASIC / PRO / ENTERPRISE) — only clients on that tier (or above)
 * are eligible to enable the feature, even if the client has paid for it.
 *
 * Example catalog:
 *   inventory.products           (BASIC,     no parent)
 *   inventory.stock_transfers    (PRO,       parent: inventory.products)
 *   inventory.barcode            (ENTERPRISE, parent: inventory.products)
 */
@Getter
@Builder
@AllArgsConstructor
public class Feature {
    private final UUID id;
    private final String code;
    private final String name;
    private final String description;
    private final String module;
    /** BASIC | PRO | ENTERPRISE */
    private final String tierRequired;
    private final String parentCode;
    private final boolean deprecated;
    private final int sortOrder;
    private final Instant createdAt;
    private final Instant updatedAt;
}

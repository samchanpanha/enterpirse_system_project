package com.reportsystem.auth.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * ClientFeature — the "is this feature on for this tenant?" record.
 *
 * The `enabled` flag can be flipped by an admin to turn a feature on/off
 * without affecting the underlying subscription tier. If a feature is not
 * present at all, it is treated as "default for the tier" (i.e. on for
 * BASIC features, off for PRO/ENTERPRISE unless explicitly enabled).
 */
@Getter
@Builder
@AllArgsConstructor
public class ClientFeature {
    private final UUID id;
    private final UUID tenantId;
    private final String featureCode;
    private boolean enabled;
    private final Instant enabledAt;
    private final UUID enabledBy;
    private final Instant expiresAt;
    private final String notes;
}

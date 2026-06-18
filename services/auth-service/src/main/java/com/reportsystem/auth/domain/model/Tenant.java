package com.reportsystem.auth.domain.model;

import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class Tenant {
    private final UUID id;
    private final String name;
    private final String slug;
    private final String domain;
    private final String logoUrl;
    private boolean active;
    private String subscription;
    private String settings;
    /** Subscription tier: BASIC, PRO, or ENTERPRISE. */
    private String tier;
    /** When the trial period ends (null if not in trial). */
    private Instant trialEndsAt;
    private final Instant createdAt;
    private Instant updatedAt;

    public void activate() {
        this.active = true;
    }

    public void deactivate() {
        this.active = false;
    }

    public void updateSubscription(String subscription) {
        this.subscription = subscription;
    }

    public void updateTier(String tier) {
        if (tier != null && (tier.equals("BASIC") || tier.equals("PRO") || tier.equals("ENTERPRISE"))) {
            this.tier = tier;
        }
    }
}

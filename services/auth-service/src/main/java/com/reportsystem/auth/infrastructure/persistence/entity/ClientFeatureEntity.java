package com.reportsystem.auth.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "client_features")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClientFeatureEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "feature_code", nullable = false, length = 100)
    private String featureCode;

    @Column(nullable = false)
    private boolean enabled;

    @Column(name = "enabled_at", nullable = false)
    private Instant enabledAt;

    @Column(name = "enabled_by")
    private UUID enabledBy;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(columnDefinition = "TEXT")
    private String notes;
}

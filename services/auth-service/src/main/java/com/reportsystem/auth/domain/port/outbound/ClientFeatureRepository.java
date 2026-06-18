package com.reportsystem.auth.domain.port.outbound;

import com.reportsystem.auth.domain.model.ClientFeature;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Outbound port for the per-client feature enablement (toggles).
 * Implementations: JpaClientFeatureRepository.
 */
public interface ClientFeatureRepository {
    ClientFeature save(ClientFeature cf);
    Optional<ClientFeature> findByTenantAndCode(UUID tenantId, String code);
    List<ClientFeature> findByTenant(UUID tenantId);
    void deleteByTenantAndCode(UUID tenantId, String code);
    /** Returns the set of enabled feature codes for a tenant. */
    java.util.Set<String> findEnabledCodesByTenant(UUID tenantId);
}

package com.reportsystem.auth.domain.port.inbound;

import com.reportsystem.auth.domain.model.Tenant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TenantUseCase {
    Tenant createTenant(String name, String slug, String domain);
    List<Tenant> getAllTenants();
    Optional<Tenant> getTenantById(UUID id);
    Optional<Tenant> getTenantBySlug(String slug);
    Tenant updateTenant(UUID id, String name, String domain, String logoUrl);
    Tenant updateTenantTier(UUID id, String tier);
    void activateTenant(UUID id);
    void deactivateTenant(UUID id);
}

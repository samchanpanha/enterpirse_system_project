package com.reportsystem.auth.domain.port.inbound;

import com.reportsystem.auth.domain.model.Tenant;
import java.util.Optional;
import java.util.UUID;

public interface TenantUseCase {
    Tenant createTenant(String name, String slug, String domain);
    Optional<Tenant> getTenantById(UUID id);
    Optional<Tenant> getTenantBySlug(String slug);
    Tenant updateTenant(UUID id, String name, String domain, String logoUrl);
    void activateTenant(UUID id);
    void deactivateTenant(UUID id);
}

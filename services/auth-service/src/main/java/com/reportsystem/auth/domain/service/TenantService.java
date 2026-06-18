package com.reportsystem.auth.domain.service;

import com.reportsystem.auth.domain.model.Tenant;
import com.reportsystem.auth.domain.port.inbound.TenantUseCase;
import com.reportsystem.auth.domain.port.outbound.TenantRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public class TenantService implements TenantUseCase {

    private final TenantRepository tenantRepository;

    public TenantService(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
    }

    @Override
    public Tenant createTenant(String name, String slug, String domain) {
        if (tenantRepository.existsBySlug(slug)) {
            throw new IllegalArgumentException("Tenant slug already exists: " + slug);
        }
        Tenant tenant = Tenant.builder()
                .id(UUID.randomUUID())
                .name(name)
                .slug(slug.toLowerCase())
                .domain(domain)
                .active(true)
                .subscription("trial")
                .settings("{}")
                .createdAt(Instant.now())
                .build();
        return tenantRepository.save(tenant);
    }

    @Override
    public Optional<Tenant> getTenantById(UUID id) {
        return tenantRepository.findById(id);
    }

    @Override
    public Optional<Tenant> getTenantBySlug(String slug) {
        return tenantRepository.findBySlug(slug);
    }

    @Override
    public Tenant updateTenant(UUID id, String name, String domain, String logoUrl) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));
        Tenant updated = Tenant.builder()
                .id(tenant.getId())
                .name(name != null ? name : tenant.getName())
                .slug(tenant.getSlug())
                .domain(domain != null ? domain : tenant.getDomain())
                .logoUrl(logoUrl != null ? logoUrl : tenant.getLogoUrl())
                .active(tenant.isActive())
                .subscription(tenant.getSubscription())
                .settings(tenant.getSettings())
                .createdAt(tenant.getCreatedAt())
                .updatedAt(Instant.now())
                .build();
        return tenantRepository.save(updated);
    }

    @Override
    public void activateTenant(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));
        tenant.activate();
        tenantRepository.save(tenant);
    }

    @Override
    public void deactivateTenant(UUID id) {
        Tenant tenant = tenantRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Tenant not found: " + id));
        tenant.deactivate();
        tenantRepository.save(tenant);
    }
}

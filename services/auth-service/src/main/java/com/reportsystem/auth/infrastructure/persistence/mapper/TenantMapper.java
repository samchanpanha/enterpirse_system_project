package com.reportsystem.auth.infrastructure.persistence.mapper;

import com.reportsystem.auth.domain.model.Tenant;
import com.reportsystem.auth.infrastructure.persistence.entity.TenantEntity;
import org.springframework.stereotype.Component;

@Component
public class TenantMapper {

    public TenantEntity toEntity(Tenant domain) {
        return TenantEntity.builder()
                .id(domain.getId())
                .name(domain.getName())
                .slug(domain.getSlug())
                .domain(domain.getDomain())
                .logoUrl(domain.getLogoUrl())
                .active(domain.isActive())
                .subscription(domain.getSubscription())
                .settings(domain.getSettings())
                .tier(domain.getTier() == null ? "BASIC" : domain.getTier())
                .trialEndsAt(domain.getTrialEndsAt())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public Tenant toDomain(TenantEntity entity) {
        return Tenant.builder()
                .id(entity.getId())
                .name(entity.getName())
                .slug(entity.getSlug())
                .domain(entity.getDomain())
                .logoUrl(entity.getLogoUrl())
                .active(entity.isActive())
                .subscription(entity.getSubscription())
                .settings(entity.getSettings())
                .tier(entity.getTier() == null ? "BASIC" : entity.getTier())
                .trialEndsAt(entity.getTrialEndsAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

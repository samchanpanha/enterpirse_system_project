package com.reportsystem.auth.infrastructure.persistence.mapper;

import com.reportsystem.auth.domain.model.ClientFeature;
import com.reportsystem.auth.infrastructure.persistence.entity.ClientFeatureEntity;
import org.springframework.stereotype.Component;

@Component
public class ClientFeatureMapper {

    public ClientFeatureEntity toEntity(ClientFeature domain) {
        return ClientFeatureEntity.builder()
                .id(domain.getId())
                .tenantId(domain.getTenantId())
                .featureCode(domain.getFeatureCode())
                .enabled(domain.isEnabled())
                .enabledAt(domain.getEnabledAt())
                .enabledBy(domain.getEnabledBy())
                .expiresAt(domain.getExpiresAt())
                .notes(domain.getNotes())
                .build();
    }

    public ClientFeature toDomain(ClientFeatureEntity entity) {
        return ClientFeature.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .featureCode(entity.getFeatureCode())
                .enabled(entity.isEnabled())
                .enabledAt(entity.getEnabledAt())
                .enabledBy(entity.getEnabledBy())
                .expiresAt(entity.getExpiresAt())
                .notes(entity.getNotes())
                .build();
    }
}

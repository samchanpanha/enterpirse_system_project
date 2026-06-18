package com.reportsystem.auth.infrastructure.persistence.mapper;

import com.reportsystem.auth.domain.model.Feature;
import com.reportsystem.auth.infrastructure.persistence.entity.FeatureEntity;
import org.springframework.stereotype.Component;

@Component
public class FeatureMapper {

    public FeatureEntity toEntity(Feature domain) {
        return FeatureEntity.builder()
                .id(domain.getId())
                .code(domain.getCode())
                .name(domain.getName())
                .description(domain.getDescription())
                .module(domain.getModule())
                .tierRequired(domain.getTierRequired())
                .parentCode(domain.getParentCode())
                .deprecated(domain.isDeprecated())
                .sortOrder(domain.getSortOrder())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public Feature toDomain(FeatureEntity entity) {
        return Feature.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .description(entity.getDescription())
                .module(entity.getModule())
                .tierRequired(entity.getTierRequired())
                .parentCode(entity.getParentCode())
                .deprecated(entity.isDeprecated())
                .sortOrder(entity.getSortOrder())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

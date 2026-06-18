package com.reportsystem.auth.infrastructure.persistence.mapper;

import com.reportsystem.auth.domain.model.Role;
import com.reportsystem.auth.infrastructure.persistence.entity.RoleEntity;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {

    public RoleEntity toEntity(Role domain) {
        return RoleEntity.builder()
                .id(domain.getId())
                .tenantId(domain.getTenantId())
                .name(domain.getName())
                .description(domain.getDescription())
                .system(domain.isSystem())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    public Role toDomain(RoleEntity entity) {
        return Role.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .name(entity.getName())
                .description(entity.getDescription())
                .system(entity.isSystem())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

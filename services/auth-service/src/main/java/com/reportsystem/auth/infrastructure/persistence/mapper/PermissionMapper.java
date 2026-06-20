package com.reportsystem.auth.infrastructure.persistence.mapper;

import com.reportsystem.auth.domain.model.Permission;
import com.reportsystem.auth.infrastructure.persistence.entity.PermissionEntity;
import org.springframework.stereotype.Component;

@Component
public class PermissionMapper {

    public PermissionEntity toEntity(Permission domain) {
        return PermissionEntity.builder()
                .id(domain.getId())
                .code(domain.getCode())
                .name(domain.getName())
                .module(domain.getModule())
                .createdAt(domain.getCreatedAt())
                .build();
    }

    public Permission toDomain(PermissionEntity entity) {
        return Permission.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .name(entity.getName())
                .module(entity.getModule())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}

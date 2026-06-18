package com.reportsystem.auth.infrastructure.persistence.mapper;

import com.reportsystem.auth.domain.model.UserBranch;
import com.reportsystem.auth.infrastructure.persistence.entity.UserBranchEntity;
import org.springframework.stereotype.Component;

@Component
public class UserBranchMapper {

    public UserBranchEntity toEntity(UserBranch domain) {
        UserBranchEntity.UserBranchId id = new UserBranchEntity.UserBranchId(
            domain.getUserId(), domain.getBranchId());
        UserBranchEntity entity = new UserBranchEntity();
        entity.setId(id);
        entity.setRole(domain.getRole() != null ? domain.getRole() : "USER");
        entity.setDefault(domain.isDefault());
        if (domain.getCreatedAt() != null) {
            entity.setCreatedAt(domain.getCreatedAt());
        }
        return entity;
    }

    public UserBranch toDomain(UserBranchEntity entity) {
        return UserBranch.builder()
            .userId(entity.getId().getUserId())
            .branchId(entity.getId().getBranchId())
            .role(entity.getRole())
            .isDefault(entity.isDefault())
            .createdAt(entity.getCreatedAt())
            .build();
    }
}

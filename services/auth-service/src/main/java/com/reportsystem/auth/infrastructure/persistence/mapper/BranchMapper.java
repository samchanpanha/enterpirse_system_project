package com.reportsystem.auth.infrastructure.persistence.mapper;

import com.reportsystem.auth.domain.model.Branch;
import com.reportsystem.auth.infrastructure.persistence.entity.BranchEntity;
import org.springframework.stereotype.Component;

@Component
public class BranchMapper {

    public BranchEntity toEntity(Branch domain) {
        return BranchEntity.builder()
                .id(domain.getId())
                .tenantId(domain.getTenantId())
                .code(domain.getCode())
                .name(domain.getName())
                .nameKh(domain.getNameKh())
                .branchType(domain.getBranchType() != null ? domain.getBranchType() : "STORE")
                .parentId(domain.getParentId())
                .address(domain.getAddress())
                .city(domain.getCity())
                .district(domain.getDistrict())
                .province(domain.getProvince())
                .phone(domain.getPhone())
                .email(domain.getEmail())
                .timezone(domain.getTimezone() != null ? domain.getTimezone() : "Asia/Phnom_Penh")
                .locale(domain.getLocale() != null ? domain.getLocale() : "km")
                .currency(domain.getCurrency() != null ? domain.getCurrency() : "USD")
                .taxRate(domain.getTaxRate())
                .logoUrl(domain.getLogoUrl())
                .settings(domain.getSettings())
                .active(domain.isActive())
                .isDefault(domain.isDefault())
                .openedAt(domain.getOpenedAt())
                .closedAt(domain.getClosedAt())
                .createdAt(domain.getCreatedAt())
                .updatedAt(domain.getUpdatedAt())
                .build();
    }

    public Branch toDomain(BranchEntity entity) {
        return Branch.builder()
                .id(entity.getId())
                .tenantId(entity.getTenantId())
                .code(entity.getCode())
                .name(entity.getName())
                .nameKh(entity.getNameKh())
                .branchType(entity.getBranchType())
                .parentId(entity.getParentId())
                .address(entity.getAddress())
                .city(entity.getCity())
                .district(entity.getDistrict())
                .province(entity.getProvince())
                .phone(entity.getPhone())
                .email(entity.getEmail())
                .timezone(entity.getTimezone())
                .locale(entity.getLocale())
                .currency(entity.getCurrency())
                .taxRate(entity.getTaxRate())
                .logoUrl(entity.getLogoUrl())
                .settings(entity.getSettings())
                .active(entity.isActive())
                .isDefault(entity.isDefault())
                .openedAt(entity.getOpenedAt())
                .closedAt(entity.getClosedAt())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

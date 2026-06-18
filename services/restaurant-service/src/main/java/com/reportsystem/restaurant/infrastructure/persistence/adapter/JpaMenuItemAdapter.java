package com.reportsystem.restaurant.infrastructure.persistence.adapter;

import com.reportsystem.restaurant.domain.model.MenuItem;
import com.reportsystem.restaurant.domain.port.outbound.MenuItemRepository;
import com.reportsystem.restaurant.infrastructure.persistence.entity.MenuItemEntity;
import com.reportsystem.restaurant.infrastructure.persistence.repository.JpaMenuItemRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaMenuItemAdapter implements MenuItemRepository {

    private final JpaMenuItemRepository repo;
    public JpaMenuItemAdapter(JpaMenuItemRepository repo) { this.repo = repo; }

    @Override
    public MenuItem save(MenuItem item) {
        MenuItemEntity e = toEntity(item);
        MenuItemEntity saved = repo.save(e);
        return toDomain(saved);
    }

    @Override
    public Optional<MenuItem> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<MenuItem> findByCategoryId(UUID categoryId) {
        return repo.findByCategoryId(categoryId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<MenuItem> findActiveByTenantId(UUID tenantId) {
        return repo.findByTenantIdAndActiveTrue(tenantId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<MenuItem> findByTenantIdAndBranchId(UUID tenantId, UUID branchId) {
        return repo.findByTenantIdAndBranchId(tenantId, branchId).stream().map(this::toDomain).toList();
    }

    private MenuItem toDomain(MenuItemEntity e) {
        return MenuItem.builder()
                .id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId()).categoryId(e.getCategoryId())
                .name(e.getName()).nameKh(e.getNameKh())
                .description(e.getDescription()).descriptionKh(e.getDescriptionKh())
                .price(e.getPrice()).currency(e.getCurrency()).taxRate(e.getTaxRate())
                .imageUrl(e.getImageUrl()).options(e.getOptions()).modifiers(e.getModifiers())
                .active(e.isActive()).sortOrder(e.getSortOrder())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }

    private MenuItemEntity toEntity(MenuItem item) {
        MenuItemEntity e = new MenuItemEntity();
        e.setId(item.getId());
        e.setTenantId(item.getTenantId());
        e.setBranchId(item.getBranchId());
        e.setCategoryId(item.getCategoryId());
        e.setName(item.getName());
        e.setNameKh(item.getNameKh());
        e.setDescription(item.getDescription());
        e.setDescriptionKh(item.getDescriptionKh());
        e.setPrice(item.getPrice());
        e.setCurrency(item.getCurrency());
        e.setTaxRate(item.getTaxRate());
        e.setImageUrl(item.getImageUrl());
        e.setOptions(item.getOptions() != null ? item.getOptions() : "[]");
        e.setModifiers(item.getModifiers() != null ? item.getModifiers() : "[]");
        e.setActive(item.isActive());
        e.setSortOrder(item.getSortOrder());
        e.setCreatedAt(item.getCreatedAt());
        e.setUpdatedAt(item.getUpdatedAt());
        return e;
    }
}

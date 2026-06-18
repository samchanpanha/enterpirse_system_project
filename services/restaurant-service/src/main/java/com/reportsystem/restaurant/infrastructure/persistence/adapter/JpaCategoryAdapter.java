package com.reportsystem.restaurant.infrastructure.persistence.adapter;

import com.reportsystem.restaurant.domain.model.Category;
import com.reportsystem.restaurant.domain.port.outbound.CategoryRepository;
import com.reportsystem.restaurant.infrastructure.persistence.entity.CategoryEntity;
import com.reportsystem.restaurant.infrastructure.persistence.repository.JpaCategoryRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaCategoryAdapter implements CategoryRepository {

    private final JpaCategoryRepository repo;
    public JpaCategoryAdapter(JpaCategoryRepository repo) { this.repo = repo; }

    @Override
    public Category save(Category category) {
        CategoryEntity e = new CategoryEntity();
        e.setId(category.getId());
        e.setTenantId(category.getTenantId());
        e.setBranchId(category.getBranchId());
        e.setOutletId(category.getOutletId());
        e.setName(category.getName());
        e.setDescription(category.getDescription());
        e.setSortOrder(category.getSortOrder());
        e.setActive(category.isActive());
        e.setCreatedAt(Instant.now());
        CategoryEntity saved = repo.save(e);
        return toDomain(saved);
    }

    @Override
    public Optional<Category> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<Category> findByTenantIdAndOutletId(UUID tenantId, UUID outletId) {
        return repo.findByTenantIdAndOutletId(tenantId, outletId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<Category> findByTenantIdAndBranchId(UUID tenantId, UUID branchId) {
        return repo.findByTenantIdAndBranchId(tenantId, branchId).stream().map(this::toDomain).toList();
    }

    private Category toDomain(CategoryEntity e) {
        return Category.builder()
                .id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId()).outletId(e.getOutletId())
                .name(e.getName()).description(e.getDescription())
                .sortOrder(e.getSortOrder()).active(e.isActive())
                .createdAt(e.getCreatedAt())
                .build();
    }
}

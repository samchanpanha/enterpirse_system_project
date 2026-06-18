package com.reportsystem.inventory.infrastructure.persistence.adapter;

import com.reportsystem.inventory.domain.model.*;
import com.reportsystem.inventory.domain.port.outbound.*;
import com.reportsystem.inventory.infrastructure.persistence.entity.*;
import com.reportsystem.inventory.infrastructure.persistence.repository.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class JpaProductCategoryAdapter implements ProductCategoryRepository {
    private final JpaProductCategoryRepository repo;
    public JpaProductCategoryAdapter(JpaProductCategoryRepository repo) { this.repo = repo; }
    @Override public ProductCategory save(ProductCategory c) {
        ProductCategoryEntity e = new ProductCategoryEntity(); e.setId(c.getId()); e.setTenantId(c.getTenantId());
        e.setName(c.getName()); e.setParentId(c.getParentId()); e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e));
    }
    @Override public Optional<ProductCategory> findById(UUID id) { return repo.findById(id).map(this::toDomain); }
    @Override public List<ProductCategory> findByTenantId(UUID t) { return repo.findByTenantId(t).stream().map(this::toDomain).toList(); }
    private ProductCategory toDomain(ProductCategoryEntity e) { return ProductCategory.builder().id(e.getId()).tenantId(e.getTenantId()).name(e.getName()).parentId(e.getParentId()).createdAt(e.getCreatedAt()).build(); }
}
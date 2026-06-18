package com.reportsystem.inventory.infrastructure.persistence.adapter;

import com.reportsystem.inventory.domain.model.*;
import com.reportsystem.inventory.domain.port.outbound.*;
import com.reportsystem.inventory.infrastructure.persistence.entity.*;
import com.reportsystem.inventory.infrastructure.persistence.repository.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class JpaProductAdapter implements ProductRepository {
    private final JpaProductRepository repo;
    public JpaProductAdapter(JpaProductRepository repo) { this.repo = repo; }
    @Override public Product save(Product p) {
        ProductEntity e = toEntity(p);
        return toDomain(repo.save(e));
    }
    @Override public Optional<Product> findById(UUID id) { return repo.findById(id).map(this::toDomain); }
    @Override public List<Product> findByTenantId(UUID t) { return repo.findByTenantId(t).stream().map(this::toDomain).toList(); }
    @Override public List<Product> findByTenantIdAndBranchId(UUID t, UUID b) { return repo.findByTenantIdAndBranchId(t, b).stream().map(this::toDomain).toList(); }
    @Override public List<Product> findByCategoryId(UUID c) { return repo.findByCategoryId(c).stream().map(this::toDomain).toList(); }
    private Product toDomain(ProductEntity e) { return Product.builder().id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId()).categoryId(e.getCategoryId()).name(e.getName()).nameKh(e.getNameKh()).sku(e.getSku()).barcode(e.getBarcode()).unit(e.getUnit()).unitPrice(e.getUnitPrice()).costPrice(e.getCostPrice()).minStock(e.getMinStock()).maxStock(e.getMaxStock()).tracked(e.isTracked()).active(e.isActive()).imageUrl(e.getImageUrl()).createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).build(); }
    private ProductEntity toEntity(Product p) { ProductEntity e = new ProductEntity(); e.setId(p.getId()); e.setTenantId(p.getTenantId()); e.setBranchId(p.getBranchId()); e.setCategoryId(p.getCategoryId()); e.setName(p.getName()); e.setNameKh(p.getNameKh()); e.setSku(p.getSku()); e.setBarcode(p.getBarcode()); e.setUnit(p.getUnit()); e.setUnitPrice(p.getUnitPrice()); e.setCostPrice(p.getCostPrice()); e.setMinStock(p.getMinStock()); e.setMaxStock(p.getMaxStock()); e.setTracked(p.isTracked()); e.setActive(p.isActive()); e.setImageUrl(p.getImageUrl()); e.setCreatedAt(p.getCreatedAt()); e.setUpdatedAt(p.getUpdatedAt()); return e; }
}
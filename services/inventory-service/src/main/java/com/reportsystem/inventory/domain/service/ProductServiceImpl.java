package com.reportsystem.inventory.domain.service;

import com.reportsystem.inventory.domain.model.*;
import com.reportsystem.inventory.domain.port.inbound.*;
import com.reportsystem.inventory.domain.port.outbound.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepo;
    public ProductServiceImpl(ProductRepository productRepo) { this.productRepo = productRepo; }

    @Override public Product createProduct(UUID tenantId, UUID branchId, UUID categoryId, String name, String sku, BigDecimal unitPrice, BigDecimal costPrice) {
        return productRepo.save(Product.builder().id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId).categoryId(categoryId)
                .name(name).sku(sku).unitPrice(unitPrice).costPrice(costPrice).unit("pcs")
                .minStock(BigDecimal.ZERO).tracked(true).active(true).createdAt(Instant.now()).build());
    }
    @Override public Optional<Product> getProductById(UUID id) { return productRepo.findById(id); }
    @Override public List<Product> getProductsByTenant(UUID tenantId) { return productRepo.findByTenantId(tenantId); }
    @Override public List<Product> getProductsByTenantAndBranch(UUID tenantId, UUID branchId) { return productRepo.findByTenantIdAndBranchId(tenantId, branchId); }
    @Override public Product updateProduct(UUID id, String name, BigDecimal price, boolean active) {
        Product p = productRepo.findById(id).orElseThrow();
        return productRepo.save(Product.builder().id(p.getId()).tenantId(p.getTenantId()).branchId(p.getBranchId()).categoryId(p.getCategoryId())
                .name(name != null ? name : p.getName()).sku(p.getSku()).unitPrice(price != null ? price : p.getUnitPrice())
                .costPrice(p.getCostPrice()).unit(p.getUnit()).minStock(p.getMinStock()).tracked(p.isTracked()).active(active)
                .createdAt(p.getCreatedAt()).updatedAt(Instant.now()).build());
    }
}
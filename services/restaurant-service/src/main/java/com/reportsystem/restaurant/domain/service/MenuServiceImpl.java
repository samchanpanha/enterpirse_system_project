package com.reportsystem.restaurant.domain.service;

import com.reportsystem.restaurant.domain.model.*;
import com.reportsystem.restaurant.domain.port.inbound.MenuService;
import com.reportsystem.restaurant.domain.port.outbound.CategoryRepository;
import com.reportsystem.restaurant.domain.port.outbound.MenuItemRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class MenuServiceImpl implements MenuService {

    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;

    public MenuServiceImpl(CategoryRepository categoryRepository, MenuItemRepository menuItemRepository) {
        this.categoryRepository = categoryRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Override
    public Category createCategory(UUID tenantId, UUID branchId, UUID outletId, String name, int sortOrder) {
        Category category = Category.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId).outletId(outletId)
                .name(name).sortOrder(sortOrder).active(true)
                .createdAt(Instant.now()).build();
        return categoryRepository.save(category);
    }

    @Override
    public List<Category> getCategories(UUID tenantId, UUID outletId) {
        return categoryRepository.findByTenantIdAndOutletId(tenantId, outletId);
    }

    @Override
    public MenuItem createMenuItem(UUID tenantId, UUID branchId, UUID categoryId, String name, BigDecimal price, BigDecimal taxRate) {
        MenuItem item = MenuItem.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId).categoryId(categoryId)
                .name(name).price(price).currency("USD")
                .taxRate(taxRate != null ? taxRate : BigDecimal.TEN)
                .options("[]").modifiers("[]").active(true)
                .createdAt(Instant.now()).build();
        return menuItemRepository.save(item);
    }

    @Override
    public Optional<MenuItem> getMenuItemById(UUID id) {
        return menuItemRepository.findById(id);
    }

    @Override
    public List<MenuItem> getMenuItemsByCategory(UUID categoryId) {
        return menuItemRepository.findByCategoryId(categoryId);
    }

    @Override
    public List<MenuItem> getActiveMenuItems(UUID tenantId, UUID outletId) {
        return menuItemRepository.findActiveByTenantId(tenantId);
    }

    @Override
    public MenuItem updateMenuItem(UUID id, String name, BigDecimal price, boolean active) {
        MenuItem existing = menuItemRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("MenuItem not found: " + id));
        MenuItem updated = MenuItem.builder()
                .id(existing.getId()).tenantId(existing.getTenantId()).categoryId(existing.getCategoryId())
                .name(name != null ? name : existing.getName())
                .nameKh(existing.getNameKh()).description(existing.getDescription())
                .price(price != null ? price : existing.getPrice())
                .currency(existing.getCurrency()).taxRate(existing.getTaxRate())
                .imageUrl(existing.getImageUrl()).options(existing.getOptions()).modifiers(existing.getModifiers())
                .active(active).sortOrder(existing.getSortOrder())
                .createdAt(existing.getCreatedAt()).updatedAt(Instant.now())
                .build();
        return menuItemRepository.save(updated);
    }

    @Override
    public void deleteMenuItem(UUID id) {
        menuItemRepository.findById(id).ifPresent(item -> {
            MenuItem deactivated = MenuItem.builder()
                    .id(item.getId()).tenantId(item.getTenantId()).categoryId(item.getCategoryId())
                    .name(item.getName()).nameKh(item.getNameKh()).description(item.getDescription())
                    .price(item.getPrice()).currency(item.getCurrency()).taxRate(item.getTaxRate())
                    .imageUrl(item.getImageUrl()).options(item.getOptions()).modifiers(item.getModifiers())
                    .active(false).sortOrder(item.getSortOrder())
                    .createdAt(item.getCreatedAt()).updatedAt(Instant.now())
                    .build();
            menuItemRepository.save(deactivated);
        });
    }
}

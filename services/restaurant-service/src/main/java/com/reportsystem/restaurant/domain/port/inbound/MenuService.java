package com.reportsystem.restaurant.domain.port.inbound;

import com.reportsystem.restaurant.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MenuService {
    Category createCategory(UUID tenantId, UUID branchId, UUID outletId, String name, int sortOrder);
    List<Category> getCategories(UUID tenantId, UUID outletId);
    MenuItem createMenuItem(UUID tenantId, UUID branchId, UUID categoryId, String name, java.math.BigDecimal price, java.math.BigDecimal taxRate);
    Optional<MenuItem> getMenuItemById(UUID id);
    List<MenuItem> getMenuItemsByCategory(UUID categoryId);
    List<MenuItem> getActiveMenuItems(UUID tenantId, UUID outletId);
    MenuItem updateMenuItem(UUID id, String name, java.math.BigDecimal price, boolean active);
    void deleteMenuItem(UUID id);
}

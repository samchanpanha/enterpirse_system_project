package com.reportsystem.restaurant.infrastructure.web;

import com.reportsystem.restaurant.domain.model.*;
import com.reportsystem.restaurant.domain.service.*;
import java.math.BigDecimal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/menu")
public class MenuController {

    private final MenuServiceImpl menuService;
    public MenuController(MenuServiceImpl menuService) { this.menuService = menuService; }

    @PostMapping("/categories")
    public ResponseEntity<Category> createCategory(@RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                                   @RequestBody Map<String, Object> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(
                menuService.createCategory(UUID.fromString((String)b.get("tenantId")),
                        bid,
                        UUID.fromString((String)b.get("outletId")), (String)b.get("name"),
                        b.get("sortOrder") != null ? ((Number)b.get("sortOrder")).intValue() : 0));
    }
    @GetMapping("/categories") public ResponseEntity<List<Category>> getCategories(@RequestParam UUID tenantId, @RequestParam UUID outletId) {
        return ResponseEntity.ok(menuService.getCategories(tenantId, outletId));
    }
    @PutMapping("/categories/{id}") public ResponseEntity<Category> updateCategory(@PathVariable UUID id, @RequestBody Map<String, Object> b) {
        return ResponseEntity.ok(menuService.updateCategory(id, (String)b.get("name"),
                b.get("sortOrder") != null ? ((Number)b.get("sortOrder")).intValue() : 0));
    }
    @DeleteMapping("/categories/{id}") public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        menuService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
    @PostMapping("/items")
    public ResponseEntity<MenuItem> createItem(@RequestHeader(value = "X-Branch-Id", required = false) String branchId,
                                              @RequestBody Map<String, Object> b) {
        UUID bid = branchId != null && !branchId.isBlank() ? UUID.fromString(branchId) : null;
        return ResponseEntity.status(HttpStatus.CREATED).body(
                menuService.createMenuItem(UUID.fromString((String)b.get("tenantId")),
                        bid,
                        UUID.fromString((String)b.get("categoryId")), (String)b.get("name"),
                        new BigDecimal(b.get("price").toString()),
                        b.get("taxRate") != null ? new BigDecimal(b.get("taxRate").toString()) : null));
    }
    @GetMapping("/items/{id}")
    public ResponseEntity<MenuItem> getItem(@PathVariable UUID id) {
        return menuService.getMenuItemById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }
    @GetMapping("/items/by-category/{categoryId}")
    public ResponseEntity<List<MenuItem>> getByCategory(@PathVariable UUID categoryId) {
        return ResponseEntity.ok(menuService.getMenuItemsByCategory(categoryId));
    }
    @GetMapping("/items/active/{tenantId}")
    public ResponseEntity<List<MenuItem>> getActive(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(menuService.getActiveMenuItems(tenantId, null));
    }
    @PutMapping("/items/{id}")
    public ResponseEntity<MenuItem> updateItem(@PathVariable UUID id, @RequestBody Map<String, Object> b) {
        return ResponseEntity.ok(menuService.updateMenuItem(id, (String)b.get("name"),
                b.get("price") != null ? new BigDecimal(b.get("price").toString()) : null,
                (boolean)b.getOrDefault("active", true)));
    }
    @DeleteMapping("/items/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID id) {
        menuService.deleteMenuItem(id);
        return ResponseEntity.noContent().build();
    }
}

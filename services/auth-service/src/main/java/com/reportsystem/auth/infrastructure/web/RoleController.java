package com.reportsystem.auth.infrastructure.web;

import com.reportsystem.auth.domain.model.Permission;
import com.reportsystem.auth.domain.model.Role;
import com.reportsystem.auth.domain.service.RoleService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/roles")
public class RoleController {

    private final RoleService roleService;

    public RoleController(RoleService roleService) {
        this.roleService = roleService;
    }

    @PostMapping
    public ResponseEntity<Role> createRole(@RequestBody Map<String, String> request) {
        Role role = roleService.createRole(
                UUID.fromString(request.get("tenantId")),
                request.get("name"),
                request.get("description"),
                Boolean.parseBoolean(request.getOrDefault("system", "false")));
        return ResponseEntity.status(HttpStatus.CREATED).body(role);
    }

    @GetMapping("/by-tenant/{tenantId}")
    public ResponseEntity<List<Role>> getRolesByTenant(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(roleService.getRolesByTenant(tenantId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Role> getRole(@PathVariable UUID id) {
        return roleService.getRoleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/permissions")
    public ResponseEntity<List<Permission>> getRolePermissions(@PathVariable UUID id) {
        return ResponseEntity.ok(roleService.getRolePermissions(id));
    }

    @PutMapping("/{id}/permissions")
    public ResponseEntity<Void> setRolePermissions(@PathVariable UUID id, @RequestBody Map<String, List<String>> request) {
        List<UUID> permissionIds = request.getOrDefault("permissionIds", List.of()).stream()
                .map(UUID::fromString)
                .toList();
        roleService.setRolePermissions(id, permissionIds);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok().build();
    }
}

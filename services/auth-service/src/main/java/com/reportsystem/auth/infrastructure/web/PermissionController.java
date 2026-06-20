package com.reportsystem.auth.infrastructure.web;

import com.reportsystem.auth.domain.model.Permission;
import com.reportsystem.auth.domain.service.PermissionService;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/permissions")
public class PermissionController {

    private final PermissionService permissionService;

    public PermissionController(PermissionService permissionService) {
        this.permissionService = permissionService;
    }

    @PostMapping
    public ResponseEntity<Permission> createPermission(@RequestBody Map<String, String> request) {
        Permission permission = permissionService.createPermission(
                request.get("code"),
                request.get("name"),
                request.get("module"));
        return ResponseEntity.status(HttpStatus.CREATED).body(permission);
    }

    @GetMapping
    public ResponseEntity<List<Permission>> getPermissions(@RequestParam Optional<String> module) {
        return ResponseEntity.ok(module.map(permissionService::getPermissionsByModule)
                .orElseGet(permissionService::getAllPermissions));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Permission> getPermission(@PathVariable UUID id) {
        return permissionService.getPermissionById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}

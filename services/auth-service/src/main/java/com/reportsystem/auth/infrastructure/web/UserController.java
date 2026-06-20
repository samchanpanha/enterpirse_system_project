package com.reportsystem.auth.infrastructure.web;

import com.reportsystem.auth.domain.model.Permission;
import com.reportsystem.auth.domain.model.Role;
import com.reportsystem.auth.domain.model.User;
import com.reportsystem.auth.domain.service.PermissionService;
import com.reportsystem.auth.domain.service.UserService;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final PermissionService permissionService;

    public UserController(UserService userService, PermissionService permissionService) {
        this.userService = userService;
        this.permissionService = permissionService;
    }

    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody Map<String, String> request) {
        User user = userService.createUser(
                UUID.fromString(request.get("tenantId")),
                request.get("email"),
                request.get("password"),
                request.get("firstName"),
                request.get("lastName"),
                request.get("phone"));
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable UUID id) {
        return userService.getUserById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/by-tenant/{tenantId}")
    public ResponseEntity<List<User>> getUsersByTenant(@PathVariable UUID tenantId) {
        return ResponseEntity.ok(userService.getUsersByTenant(tenantId));
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable UUID id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> resetPassword(@PathVariable UUID id, @RequestBody Map<String, String> request) {
        userService.updateUserPassword(id, request.get("password"));
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/roles")
    public ResponseEntity<Void> assignRoles(@PathVariable UUID id, @RequestBody Map<String, List<String>> request) {
        List<UUID> roleIds = request.getOrDefault("roleIds", List.of()).stream()
                .map(UUID::fromString)
                .toList();
        userService.assignRoles(id, roleIds);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/roles")
    public ResponseEntity<List<Role>> getUserRoles(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.getUserRoles(id));
    }

    @GetMapping("/me/permissions")
    public ResponseEntity<List<String>> getMyPermissions(@RequestHeader("X-User-Id") String userId) {
        List<UUID> permissionIds = userService.getUserPermissionIds(UUID.fromString(userId));
        List<String> codes = permissionService.getAllPermissions().stream()
                .filter(p -> permissionIds.contains(p.getId()))
                .map(Permission::getCode)
                .toList();
        return ResponseEntity.ok(codes);
    }
}

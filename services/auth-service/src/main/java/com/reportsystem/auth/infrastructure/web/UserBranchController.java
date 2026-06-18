package com.reportsystem.auth.infrastructure.web;

import com.reportsystem.auth.domain.model.UserBranch;
import com.reportsystem.auth.domain.service.UserBranchService;
import com.reportsystem.auth.domain.service.UserService;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user-branches")
public class UserBranchController {

    private final UserBranchService userBranchService;
    private final UserService userService;

    public UserBranchController(UserBranchService userBranchService, UserService userService) {
        this.userBranchService = userBranchService;
        this.userService = userService;
    }

    @GetMapping("/by-user/{userId}")
    public ResponseEntity<List<UserBranch>> listByUser(@PathVariable UUID userId) {
        return ResponseEntity.ok(userBranchService.getUserBranches(userId));
    }

    /**
     * Resolve a user's local branches by email + tenant (for Keycloak-bridged auth
     * where the X-User-Id header is the Keycloak sub, not the local user UUID).
     */
    @GetMapping("/by-email")
    public ResponseEntity<List<UserBranch>> listByEmail(
            @RequestParam String email,
            @RequestParam UUID tenantId) {
        var userOpt = userService.getUserByEmail(tenantId, email);
        if (userOpt.isEmpty()) {
            return ResponseEntity.ok(java.util.List.of());
        }
        return ResponseEntity.ok(userBranchService.getUserBranches(userOpt.get().getId()));
    }

    @GetMapping("/by-branch/{branchId}")
    public ResponseEntity<List<UserBranch>> listByBranch(@PathVariable UUID branchId) {
        return ResponseEntity.ok(userBranchService.getBranchUsers(branchId));
    }

    @PostMapping
    public ResponseEntity<UserBranch> assign(@RequestBody Map<String, Object> body) {
        UUID userId = UUID.fromString((String) body.get("userId"));
        UUID branchId = UUID.fromString((String) body.get("branchId"));
        String role = (String) body.getOrDefault("role", "USER");
        boolean isDefault = Boolean.TRUE.equals(body.get("isDefault"));
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(userBranchService.assign(userId, branchId, role, isDefault));
    }

    @PutMapping("/{userId}/{branchId}")
    public ResponseEntity<UserBranch> update(
            @PathVariable UUID userId,
            @PathVariable UUID branchId,
            @RequestBody Map<String, Object> body) {
        // Reuse assign: throw if not exists
        userBranchService.unassign(userId, branchId);
        String role = (String) body.getOrDefault("role", "USER");
        boolean isDefault = Boolean.TRUE.equals(body.get("isDefault"));
        return ResponseEntity.ok(userBranchService.assign(userId, branchId, role, isDefault));
    }

    @DeleteMapping("/{userId}/{branchId}")
    public ResponseEntity<?> unassign(@PathVariable UUID userId, @PathVariable UUID branchId) {
        userBranchService.unassign(userId, branchId);
        return ResponseEntity.noContent().build();
    }
}

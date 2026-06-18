package com.reportsystem.shared.security;

import java.util.List;
import java.util.Set;

public class PermissionEvaluator {

    private final Set<String> userPermissions;

    public PermissionEvaluator(Set<String> userPermissions) {
        this.userPermissions = userPermissions;
    }

    public static PermissionEvaluator fromRoles(List<String> roles, RolePermissionRegistry registry) {
        Set<String> permissions = registry.getPermissionsForRoles(roles);
        return new PermissionEvaluator(permissions);
    }

    public boolean hasPermission(String permissionCode) {
        return userPermissions.contains(permissionCode);
    }

    public boolean hasAnyPermission(String... permissionCodes) {
        for (String code : permissionCodes) {
            if (userPermissions.contains(code)) {
                return true;
            }
        }
        return false;
    }

    public boolean hasAllPermissions(String... permissionCodes) {
        for (String code : permissionCodes) {
            if (!userPermissions.contains(code)) {
                return false;
            }
        }
        return true;
    }

    public Set<String> getPermissions() {
        return Set.copyOf(userPermissions);
    }

    public interface RolePermissionRegistry {
        Set<String> getPermissionsForRoles(List<String> roles);
    }
}

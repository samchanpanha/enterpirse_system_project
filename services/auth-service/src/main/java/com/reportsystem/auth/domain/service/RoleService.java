package com.reportsystem.auth.domain.service;

import com.reportsystem.auth.domain.model.Permission;
import com.reportsystem.auth.domain.model.Role;
import com.reportsystem.auth.domain.port.outbound.PermissionRepository;
import com.reportsystem.auth.domain.port.outbound.RoleRepository;
import com.reportsystem.auth.infrastructure.persistence.entity.RolePermissionEntity;
import com.reportsystem.auth.infrastructure.persistence.repository.JpaRolePermissionRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RoleService {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final JpaRolePermissionRepository rolePermissionRepository;

    public RoleService(RoleRepository roleRepository,
                       PermissionRepository permissionRepository,
                       JpaRolePermissionRepository rolePermissionRepository) {
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
        this.rolePermissionRepository = rolePermissionRepository;
    }

    public Role createRole(UUID tenantId, String name, String description, boolean system) {
        if (roleRepository.findByNameAndTenantId(name, tenantId).isPresent()) {
            throw new IllegalArgumentException("Role already exists for this tenant: " + name);
        }
        Role role = Role.builder()
                .id(UUID.randomUUID())
                .tenantId(tenantId)
                .name(name)
                .description(description)
                .system(system)
                .createdAt(Instant.now())
                .build();
        return roleRepository.save(role);
    }

    public List<Role> getRolesByTenant(UUID tenantId) {
        return roleRepository.findByTenantId(tenantId);
    }

    public Optional<Role> getRoleById(UUID id) {
        return roleRepository.findById(id);
    }

    @Transactional
    public void setRolePermissions(UUID roleId, List<UUID> permissionIds) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        if (role.isSystem()) {
            throw new IllegalArgumentException("Cannot modify system role permissions: " + role.getName());
        }
        rolePermissionRepository.deleteByIdRoleId(roleId);
        for (UUID permissionId : permissionIds) {
            if (!permissionRepository.findById(permissionId).isPresent()) {
                throw new IllegalArgumentException("Permission not found: " + permissionId);
            }
            rolePermissionRepository.save(new RolePermissionEntity(roleId, permissionId));
        }
    }

    public List<Permission> getRolePermissions(UUID roleId) {
        List<UUID> permissionIds = rolePermissionRepository.findByIdRoleId(roleId).stream()
                .map(RolePermissionEntity::getPermissionId)
                .toList();
        return permissionRepository.findByIds(permissionIds);
    }

    @Transactional
    public void deleteRole(UUID roleId) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        if (role.isSystem()) {
            throw new IllegalArgumentException("Cannot delete system role: " + role.getName());
        }
        rolePermissionRepository.deleteByIdRoleId(roleId);
        // User roles referencing this role will be deleted by FK cascade
    }
}

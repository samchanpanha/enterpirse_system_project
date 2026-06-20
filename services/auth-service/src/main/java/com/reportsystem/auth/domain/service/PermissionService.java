package com.reportsystem.auth.domain.service;

import com.reportsystem.auth.domain.model.Permission;
import com.reportsystem.auth.domain.port.outbound.PermissionRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class PermissionService {

    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public Permission createPermission(String code, String name, String module) {
        if (permissionRepository.findByCode(code).isPresent()) {
            throw new IllegalArgumentException("Permission code already exists: " + code);
        }
        Permission permission = Permission.builder()
                .id(UUID.randomUUID())
                .code(code)
                .name(name)
                .module(module)
                .createdAt(Instant.now())
                .build();
        return permissionRepository.save(permission);
    }

    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    public List<Permission> getPermissionsByModule(String module) {
        return permissionRepository.findByModule(module);
    }

    public Optional<Permission> getPermissionById(UUID id) {
        return permissionRepository.findById(id);
    }

    public Optional<Permission> getPermissionByCode(String code) {
        return permissionRepository.findByCode(code);
    }
}

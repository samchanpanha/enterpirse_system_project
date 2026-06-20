package com.reportsystem.auth.domain.port.outbound;

import com.reportsystem.auth.domain.model.Permission;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PermissionRepository {
    Permission save(Permission permission);
    Optional<Permission> findById(UUID id);
    Optional<Permission> findByCode(String code);
    List<Permission> findAll();
    List<Permission> findByModule(String module);
    List<Permission> findByIds(List<UUID> ids);
}

package com.reportsystem.auth.domain.port.outbound;

import com.reportsystem.auth.domain.model.Role;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RoleRepository {
    Role save(Role role);
    Optional<Role> findById(UUID id);
    List<Role> findByTenantId(UUID tenantId);
    Optional<Role> findByNameAndTenantId(String name, UUID tenantId);
}

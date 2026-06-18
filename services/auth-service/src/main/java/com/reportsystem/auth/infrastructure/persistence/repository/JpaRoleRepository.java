package com.reportsystem.auth.infrastructure.persistence.repository;

import com.reportsystem.auth.infrastructure.persistence.entity.RoleEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaRoleRepository extends JpaRepository<RoleEntity, UUID> {
    List<RoleEntity> findByTenantId(UUID tenantId);
    Optional<RoleEntity> findByNameAndTenantId(String name, UUID tenantId);
}

package com.reportsystem.auth.infrastructure.persistence.repository;

import com.reportsystem.auth.infrastructure.persistence.entity.UserEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRepository extends JpaRepository<UserEntity, UUID> {
    Optional<UserEntity> findByEmailAndTenantId(String email, UUID tenantId);
    Optional<UserEntity> findByEmail(String email);
    List<UserEntity> findByTenantId(UUID tenantId);
    boolean existsByEmailAndTenantId(String email, UUID tenantId);
}

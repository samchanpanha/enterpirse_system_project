package com.reportsystem.auth.infrastructure.persistence.repository;

import com.reportsystem.auth.infrastructure.persistence.entity.UserRoleEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserRoleRepository extends JpaRepository<UserRoleEntity, UserRoleEntity.UserRoleId> {
    List<UserRoleEntity> findByIdUserId(UUID userId);
    void deleteByIdUserId(UUID userId);
}

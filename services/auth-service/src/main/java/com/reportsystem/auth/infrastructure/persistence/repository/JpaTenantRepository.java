package com.reportsystem.auth.infrastructure.persistence.repository;

import com.reportsystem.auth.infrastructure.persistence.entity.TenantEntity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaTenantRepository extends JpaRepository<TenantEntity, UUID> {
    Optional<TenantEntity> findBySlug(String slug);
    boolean existsBySlug(String slug);
}

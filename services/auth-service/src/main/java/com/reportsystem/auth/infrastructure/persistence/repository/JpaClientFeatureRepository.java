package com.reportsystem.auth.infrastructure.persistence.repository;

import com.reportsystem.auth.infrastructure.persistence.entity.ClientFeatureEntity;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaClientFeatureRepository extends JpaRepository<ClientFeatureEntity, UUID> {
    List<ClientFeatureEntity> findByTenantId(UUID tenantId);
    Optional<ClientFeatureEntity> findByTenantIdAndFeatureCode(UUID tenantId, String featureCode);
    void deleteByTenantIdAndFeatureCode(UUID tenantId, String featureCode);

    @Query("SELECT c.featureCode FROM ClientFeatureEntity c WHERE c.tenantId = :tenantId AND c.enabled = true")
    Set<String> findEnabledCodesByTenantId(UUID tenantId);
}

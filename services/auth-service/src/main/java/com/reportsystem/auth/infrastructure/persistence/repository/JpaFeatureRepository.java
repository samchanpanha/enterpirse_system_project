package com.reportsystem.auth.infrastructure.persistence.repository;

import com.reportsystem.auth.infrastructure.persistence.entity.FeatureEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaFeatureRepository extends JpaRepository<FeatureEntity, UUID> {
    Optional<FeatureEntity> findByCode(String code);
    List<FeatureEntity> findByModuleOrderBySortOrder(String module);
    List<FeatureEntity> findAllByOrderByModuleAscSortOrderAsc();
}

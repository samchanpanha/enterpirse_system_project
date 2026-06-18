package com.reportsystem.auth.infrastructure.persistence.repository;

import com.reportsystem.auth.infrastructure.persistence.entity.UserBranchEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaUserBranchRepository extends JpaRepository<UserBranchEntity, UserBranchEntity.UserBranchId> {
    List<UserBranchEntity> findByIdUserId(UUID userId);
    List<UserBranchEntity> findByIdBranchId(UUID branchId);
}

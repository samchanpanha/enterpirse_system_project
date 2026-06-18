package com.reportsystem.auth.domain.port.outbound;

import com.reportsystem.auth.domain.model.UserBranch;
import java.util.List;
import java.util.UUID;

public interface UserBranchRepository {
    UserBranch save(UserBranch userBranch);
    void delete(UUID userId, UUID branchId);
    List<UserBranch> findByUserId(UUID userId);
    List<UserBranch> findByBranchId(UUID branchId);
    List<UserBranch> findByTenantId(UUID tenantId);
    boolean existsByUserIdAndBranchId(UUID userId, UUID branchId);
}

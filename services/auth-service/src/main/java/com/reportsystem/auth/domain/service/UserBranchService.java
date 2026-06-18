package com.reportsystem.auth.domain.service;

import com.reportsystem.auth.domain.model.UserBranch;
import com.reportsystem.auth.domain.port.outbound.UserBranchRepository;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserBranchService {

    private final UserBranchRepository userBranchRepository;

    public UserBranchService(UserBranchRepository userBranchRepository) {
        this.userBranchRepository = userBranchRepository;
    }

    @Transactional
    public UserBranch assign(UUID userId, UUID branchId, String role, boolean isDefault) {
        if (userBranchRepository.existsByUserIdAndBranchId(userId, branchId)) {
            throw new IllegalStateException("User " + userId + " is already assigned to branch " + branchId);
        }
        UserBranch ub = UserBranch.builder()
            .userId(userId)
            .branchId(branchId)
            .role(role != null ? role : "USER")
            .isDefault(isDefault)
            .createdAt(Instant.now())
            .build();
        return userBranchRepository.save(ub);
    }

    @Transactional
    public void unassign(UUID userId, UUID branchId) {
        userBranchRepository.delete(userId, branchId);
    }

    public List<UserBranch> getUserBranches(UUID userId) {
        return userBranchRepository.findByUserId(userId);
    }

    public List<UserBranch> getBranchUsers(UUID branchId) {
        return userBranchRepository.findByBranchId(branchId);
    }
}

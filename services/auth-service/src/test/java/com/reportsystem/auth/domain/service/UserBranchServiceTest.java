package com.reportsystem.auth.domain.service;

import com.reportsystem.auth.domain.model.UserBranch;
import com.reportsystem.auth.domain.port.outbound.UserBranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserBranchService")
class UserBranchServiceTest {

    @Mock private UserBranchRepository userBranchRepository;
    private UserBranchService userBranchService;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000aaa");
    private static final UUID BRANCH_ID = UUID.fromString("00000000-0000-0000-0000-000000000bbb");

    @BeforeEach
    void setUp() {
        userBranchService = new UserBranchService(userBranchRepository);
    }

    @Test
    @DisplayName("assign creates UserBranch with provided role and default flag")
    void assign_happyPath() {
        when(userBranchRepository.existsByUserIdAndBranchId(USER_ID, BRANCH_ID)).thenReturn(false);
        when(userBranchRepository.save(any(UserBranch.class))).thenAnswer(inv -> inv.getArgument(0));

        UserBranch ub = userBranchService.assign(USER_ID, BRANCH_ID, "BRANCH_MANAGER", true);

        assertThat(ub.getUserId()).isEqualTo(USER_ID);
        assertThat(ub.getBranchId()).isEqualTo(BRANCH_ID);
        assertThat(ub.getRole()).isEqualTo("BRANCH_MANAGER");
        assertThat(ub.isDefault()).isTrue();
        assertThat(ub.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("assign defaults role to USER when role is null")
    void assign_defaultsRoleToUser() {
        when(userBranchRepository.existsByUserIdAndBranchId(USER_ID, BRANCH_ID)).thenReturn(false);
        when(userBranchRepository.save(any(UserBranch.class))).thenAnswer(inv -> inv.getArgument(0));

        UserBranch ub = userBranchService.assign(USER_ID, BRANCH_ID, null, false);

        assertThat(ub.getRole()).isEqualTo("USER");
    }

    @Test
    @DisplayName("assign rejects duplicate assignment")
    void assign_duplicateThrows() {
        when(userBranchRepository.existsByUserIdAndBranchId(USER_ID, BRANCH_ID)).thenReturn(true);

        assertThatThrownBy(() -> userBranchService.assign(USER_ID, BRANCH_ID, "USER", false))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("already assigned");

        verify(userBranchRepository, never()).save(any());
    }

    @Test
    @DisplayName("unassign delegates to repository")
    void unassign_delegates() {
        userBranchService.unassign(USER_ID, BRANCH_ID);
        verify(userBranchRepository).delete(USER_ID, BRANCH_ID);
    }

    @Test
    @DisplayName("getUserBranches returns assignments for user")
    void getUserBranches_delegates() {
        UserBranch ub = UserBranch.builder()
            .userId(USER_ID).branchId(BRANCH_ID).role("USER").isDefault(true).build();
        when(userBranchRepository.findByUserId(USER_ID)).thenReturn(List.of(ub));

        List<UserBranch> result = userBranchService.getUserBranches(USER_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBranchId()).isEqualTo(BRANCH_ID);
    }

    @Test
    @DisplayName("getBranchUsers returns assignments for branch")
    void getBranchUsers_delegates() {
        UserBranch ub = UserBranch.builder()
            .userId(USER_ID).branchId(BRANCH_ID).role("USER").isDefault(false).build();
        when(userBranchRepository.findByBranchId(BRANCH_ID)).thenReturn(List.of(ub));

        List<UserBranch> result = userBranchService.getBranchUsers(BRANCH_ID);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserId()).isEqualTo(USER_ID);
    }
}

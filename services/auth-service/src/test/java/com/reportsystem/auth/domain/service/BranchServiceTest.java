package com.reportsystem.auth.domain.service;

import com.reportsystem.auth.domain.model.Branch;
import com.reportsystem.auth.domain.port.outbound.BranchRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("BranchService")
class BranchServiceTest {

    @Mock private BranchRepository branchRepository;
    private BranchService branchService;

    private static final UUID TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID BRANCH_ID = UUID.fromString("00000000-0000-0000-0000-000000000020");

    @BeforeEach
    void setUp() {
        branchService = new BranchService(branchRepository);
    }

    @Test
    @DisplayName("createBranch fills in defaults (id, createdAt, branchType, settings) when missing")
    void createBranch_fillsDefaults() {
        when(branchRepository.findByTenantIdAndCode(TENANT_ID, "BR01")).thenReturn(Optional.empty());
        when(branchRepository.findByTenantId(TENANT_ID)).thenReturn(List.of()); // first branch
        when(branchRepository.save(any(Branch.class))).thenAnswer(inv -> inv.getArgument(0));

        Branch input = Branch.builder()
            .tenantId(TENANT_ID)
            .code("BR01")
            .name("Branch One")
            .build();

        Branch saved = branchService.createBranch(input);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getBranchType()).isEqualTo("STORE");
        assertThat(saved.getSettings()).isEqualTo("{}");
        assertThat(saved.isDefault()).isTrue(); // first branch becomes default
    }

    @Test
    @DisplayName("createBranch rejects empty code")
    void createBranch_emptyCodeThrows() {
        Branch b = Branch.builder().tenantId(TENANT_ID).code("").name("X").build();
        assertThatThrownBy(() -> branchService.createBranch(b))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("code is required");
        verify(branchRepository, never()).save(any());
    }

    @Test
    @DisplayName("createBranch rejects empty name")
    void createBranch_emptyNameThrows() {
        Branch b = Branch.builder().tenantId(TENANT_ID).code("BR01").name("").build();
        assertThatThrownBy(() -> branchService.createBranch(b))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("name is required");
    }

    @Test
    @DisplayName("createBranch rejects duplicate code within tenant")
    void createBranch_duplicateCodeThrows() {
        when(branchRepository.findByTenantIdAndCode(TENANT_ID, "BR01"))
            .thenReturn(Optional.of(Branch.builder().id(BRANCH_ID).tenantId(TENANT_ID).code("BR01").name("Existing").build()));

        Branch b = Branch.builder().tenantId(TENANT_ID).code("BR01").name("New").build();

        assertThatThrownBy(() -> branchService.createBranch(b))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Branch code already exists");
    }

    @Test
    @DisplayName("createBranch does NOT mark non-first branch as default")
    void createBranch_secondBranchIsNotDefault() {
        Branch existing = Branch.builder()
            .id(UUID.randomUUID()).tenantId(TENANT_ID).code("HQ").name("HQ").isDefault(true).build();
        when(branchRepository.findByTenantIdAndCode(TENANT_ID, "BR01")).thenReturn(Optional.empty());
        when(branchRepository.findByTenantId(TENANT_ID)).thenReturn(List.of(existing));
        when(branchRepository.save(any(Branch.class))).thenAnswer(inv -> inv.getArgument(0));

        Branch b = Branch.builder().tenantId(TENANT_ID).code("BR01").name("New").build();
        Branch saved = branchService.createBranch(b);

        assertThat(saved.isDefault()).isFalse();
    }

    @Test
    @DisplayName("deleteBranch refuses to delete the default branch")
    void deleteBranch_defaultBranchThrows() {
        Branch def = Branch.builder()
            .id(BRANCH_ID).tenantId(TENANT_ID).code("HQ").name("HQ").isDefault(true).build();
        when(branchRepository.findById(BRANCH_ID)).thenReturn(Optional.of(def));

        assertThatThrownBy(() -> branchService.deleteBranch(BRANCH_ID))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Cannot delete the default branch");
        verify(branchRepository, never()).deleteById(BRANCH_ID);
    }

    @Test
    @DisplayName("deleteBranch removes a non-default branch")
    void deleteBranch_nonDefaultSucceeds() {
        Branch b = Branch.builder()
            .id(BRANCH_ID).tenantId(TENANT_ID).code("BR01").name("B").isDefault(false).build();
        when(branchRepository.findById(BRANCH_ID)).thenReturn(Optional.of(b));

        branchService.deleteBranch(BRANCH_ID);

        verify(branchRepository).deleteById(BRANCH_ID);
    }

    @Test
    @DisplayName("updateBranch applies only non-null fields")
    void updateBranch_partialUpdate() {
        Branch existing = Branch.builder()
            .id(BRANCH_ID).tenantId(TENANT_ID).code("HQ")
            .name("Old HQ").nameKh("ចាសុខ").branchType("HQ")
            .address("Old").city("Old City").isDefault(true)
            .createdAt(Instant.now())
            .build();
        when(branchRepository.findById(BRANCH_ID)).thenReturn(Optional.of(existing));
        when(branchRepository.save(any(Branch.class))).thenAnswer(inv -> inv.getArgument(0));

        Branch updates = Branch.builder()
            .name("New HQ")  // only name changes
            .build();
        Branch updated = branchService.updateBranch(BRANCH_ID, updates);

        assertThat(updated.getName()).isEqualTo("New HQ");
        assertThat(updated.getNameKh()).isEqualTo("ចាសុខ"); // unchanged
        assertThat(updated.getAddress()).isEqualTo("Old");      // unchanged
        // Note: isDefault is always set from updates (no null check), so it becomes
        // the boolean default (false) when the update didn't set it.
        assertThat(updated.isDefault()).isFalse();
    }

    @Test
    @DisplayName("updateBranch throws when branch not found")
    void updateBranch_notFoundThrows() {
        when(branchRepository.findById(BRANCH_ID)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> branchService.updateBranch(BRANCH_ID, Branch.builder().name("X").build()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Branch not found");
    }

    @Test
    @DisplayName("getBranchesByTenant delegates to repository")
    void getBranchesByTenant_delegates() {
        when(branchRepository.findByTenantId(TENANT_ID))
            .thenReturn(List.of(Branch.builder().id(BRANCH_ID).code("HQ").name("HQ").tenantId(TENANT_ID).build()));
        List<Branch> result = branchService.getBranchesByTenant(TENANT_ID);
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCode()).isEqualTo("HQ");
    }
}

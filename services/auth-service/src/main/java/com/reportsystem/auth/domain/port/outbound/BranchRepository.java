package com.reportsystem.auth.domain.port.outbound;

import com.reportsystem.auth.domain.model.Branch;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BranchRepository {
    Branch save(Branch branch);
    Optional<Branch> findById(UUID id);
    List<Branch> findByTenantId(UUID tenantId);
    Optional<Branch> findByTenantIdAndCode(UUID tenantId, String code);
    boolean existsById(UUID id);
    void deleteById(UUID id);
}

package com.reportsystem.finance.domain.port.outbound;

import com.reportsystem.finance.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepository {
    Account save(Account a);
    Optional<Account> findById(UUID id);
    List<Account> findByTenantId(UUID tenantId);
    List<Account> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

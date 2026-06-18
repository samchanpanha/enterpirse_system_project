package com.reportsystem.payment.domain.port.outbound;

import com.reportsystem.payment.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReconciliationRecordRepository {
    ReconciliationRecord save(ReconciliationRecord r);
    Optional<ReconciliationRecord> findById(UUID id);
    List<ReconciliationRecord> findByTenantId(UUID tenantId);
    List<ReconciliationRecord> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
}

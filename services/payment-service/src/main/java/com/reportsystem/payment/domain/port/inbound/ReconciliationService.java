package com.reportsystem.payment.domain.port.inbound;

import com.reportsystem.payment.domain.model.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ReconciliationService {
    ReconciliationRecord startReconciliation(UUID tenantId, UUID branchId, String gateway, java.time.LocalDate date);
    ReconciliationRecord completeReconciliation(UUID id);
    List<ReconciliationRecord> findByTenant(UUID tenantId);
    List<ReconciliationRecord> findByTenantAndBranch(UUID tenantId, UUID branchId);
}

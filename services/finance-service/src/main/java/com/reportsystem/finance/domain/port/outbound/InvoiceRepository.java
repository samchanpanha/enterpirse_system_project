package com.reportsystem.finance.domain.port.outbound;

import com.reportsystem.finance.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository {
    Invoice save(Invoice i);
    Optional<Invoice> findById(UUID id);
    List<Invoice> findByTenantId(UUID tenantId);
    List<Invoice> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    String generateInvoiceNumber(UUID tenantId);
}

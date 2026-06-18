package com.reportsystem.payment.domain.port.outbound;

import com.reportsystem.payment.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository {
    Transaction save(Transaction t);
    Optional<Transaction> findById(UUID id);
    List<Transaction> findByTenantId(UUID tenantId);
    List<Transaction> findByTenantIdAndBranchId(UUID tenantId, UUID branchId);
    List<Transaction> findByInvoiceId(UUID invoiceId);
    String generateTransactionId(UUID tenantId);
}

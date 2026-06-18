package com.reportsystem.payment.domain.port.inbound;

import com.reportsystem.payment.domain.model.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentService {
    Transaction createTransaction(UUID tenantId, UUID branchId, UUID invoiceId, BigDecimal amount, String currency, String gateway, String method,
                                   String customerName, String customerPhone, String sourceType, UUID sourceId);
    Optional<Transaction> getTransactionById(UUID id);
    List<Transaction> getTransactionsByTenant(UUID tenantId);
    List<Transaction> getTransactionsByTenantAndBranch(UUID tenantId, UUID branchId);
    Transaction processPayment(UUID transactionId);
    Transaction refundTransaction(UUID transactionId, BigDecimal amount, String reason, UUID createdBy);
}

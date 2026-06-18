package com.reportsystem.payment.infrastructure.persistence.repository;

import com.reportsystem.payment.infrastructure.persistence.entity.*;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaTransactionRepository extends JpaRepository<TransactionEntity, UUID> {
    List<TransactionEntity> findByTenantId(UUID t); List<TransactionEntity> findByInvoiceId(UUID id);
    List<TransactionEntity> findByTenantIdAndBranchId(UUID t, UUID b);
}

package com.reportsystem.payment.infrastructure.persistence.repository;

import com.reportsystem.payment.infrastructure.persistence.entity.*;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaReconciliationRecordRepository extends JpaRepository<ReconciliationRecordEntity, UUID> {
    List<ReconciliationRecordEntity> findByTenantId(UUID t);
    List<ReconciliationRecordEntity> findByTenantIdAndBranchId(UUID t, UUID b);
}

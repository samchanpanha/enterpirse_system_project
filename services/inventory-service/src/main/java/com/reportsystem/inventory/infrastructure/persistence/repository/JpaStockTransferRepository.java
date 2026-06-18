package com.reportsystem.inventory.infrastructure.persistence.repository;

import com.reportsystem.inventory.infrastructure.persistence.entity.StockTransferEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaStockTransferRepository extends JpaRepository<StockTransferEntity, UUID> {
    @Query("SELECT t FROM StockTransferEntity t WHERE t.tenantId = ?1 ORDER BY t.createdAt DESC")
    List<StockTransferEntity> findByTenantIdOrderByCreatedAtDesc(UUID tenantId);

    @Query("SELECT t FROM StockTransferEntity t WHERE t.tenantId = ?1 AND t.status = ?2 ORDER BY t.createdAt DESC")
    List<StockTransferEntity> findByTenantIdAndStatus(UUID tenantId, String status);

    @Query("SELECT t FROM StockTransferEntity t WHERE t.tenantId = ?1 AND (t.fromBranchId = ?2 OR t.toBranchId = ?2) ORDER BY t.createdAt DESC")
    List<StockTransferEntity> findByTenantIdAndBranch(UUID tenantId, UUID branchId);

    @Query("SELECT t FROM StockTransferEntity t WHERE t.fromBranchId = ?1 AND t.status = 'SHIPPED' ORDER BY t.createdAt DESC")
    List<StockTransferEntity> findIncomingForBranch(UUID branchId);

    Optional<StockTransferEntity> findByTenantIdAndTransferNumber(UUID tenantId, String transferNumber);

    long countByTenantIdAndStatus(UUID tenantId, String status);
}

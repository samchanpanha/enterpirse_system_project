package com.reportsystem.payment.infrastructure.persistence.adapter;

import com.reportsystem.payment.domain.model.*;
import com.reportsystem.payment.domain.port.outbound.*;
import com.reportsystem.payment.infrastructure.persistence.entity.*;
import com.reportsystem.payment.infrastructure.persistence.repository.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class JpaReconciliationRecordAdapter implements ReconciliationRecordRepository {
    private final JpaReconciliationRecordRepository repo;
    public JpaReconciliationRecordAdapter(JpaReconciliationRecordRepository r) { repo = r; }
    @Override public ReconciliationRecord save(ReconciliationRecord r) {
        ReconciliationRecordEntity e = new ReconciliationRecordEntity(); e.setId(r.getId()); e.setTenantId(r.getTenantId()); e.setBranchId(r.getBranchId());
        e.setGateway(r.getGateway()); e.setStatementDate(r.getStatementDate()); e.setTotalExpected(r.getTotalExpected());
        e.setTotalMatched(r.getTotalMatched()); e.setTotalUnmatched(r.getTotalUnmatched()); e.setStatus(r.getStatus());
        e.setMatchedCount(r.getMatchedCount()); e.setUnmatchedCount(r.getUnmatchedCount()); e.setProcessedAt(r.getProcessedAt()); e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e)); }
    @Override public Optional<ReconciliationRecord> findById(UUID id) { return repo.findById(id).map(this::toDomain); }
    @Override public List<ReconciliationRecord> findByTenantId(UUID t) { return repo.findByTenantId(t).stream().map(this::toDomain).toList(); }
    @Override public List<ReconciliationRecord> findByTenantIdAndBranchId(UUID t, UUID b) { return repo.findByTenantIdAndBranchId(t, b).stream().map(this::toDomain).toList(); }
    private ReconciliationRecord toDomain(ReconciliationRecordEntity e) { return ReconciliationRecord.builder().id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId()).gateway(e.getGateway()).statementDate(e.getStatementDate()).totalExpected(e.getTotalExpected()).totalMatched(e.getTotalMatched()).totalUnmatched(e.getTotalUnmatched()).status(e.getStatus()).matchedCount(e.getMatchedCount()).unmatchedCount(e.getUnmatchedCount()).processedAt(e.getProcessedAt()).createdAt(e.getCreatedAt()).build(); }
}
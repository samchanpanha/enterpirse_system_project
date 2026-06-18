package com.reportsystem.payment.domain.service;

import com.reportsystem.payment.domain.model.*;
import com.reportsystem.payment.domain.port.inbound.*;
import com.reportsystem.payment.domain.port.outbound.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class ReconciliationServiceImpl implements ReconciliationService {
    private final ReconciliationRecordRepository recRepo;
    public ReconciliationServiceImpl(ReconciliationRecordRepository recRepo) { this.recRepo = recRepo; }
    @Override public ReconciliationRecord startReconciliation(UUID tenantId, UUID branchId, String gateway, java.time.LocalDate date) {
        return recRepo.save(ReconciliationRecord.builder().id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId).gateway(gateway).statementDate(date).totalExpected(BigDecimal.ZERO).totalMatched(BigDecimal.ZERO).totalUnmatched(BigDecimal.ZERO).status("pending").createdAt(Instant.now()).build());
    }
    @Override public ReconciliationRecord completeReconciliation(UUID id) {
        ReconciliationRecord r = recRepo.findById(id).orElseThrow();
        return recRepo.save(ReconciliationRecord.builder().id(r.getId()).tenantId(r.getTenantId()).branchId(r.getBranchId()).gateway(r.getGateway()).statementDate(r.getStatementDate()).totalExpected(r.getTotalExpected()).totalMatched(r.getTotalMatched()).totalUnmatched(r.getTotalUnmatched()).status("completed").matchedCount(r.getMatchedCount()).unmatchedCount(r.getUnmatchedCount()).processedAt(Instant.now()).createdAt(r.getCreatedAt()).build());
    }
}
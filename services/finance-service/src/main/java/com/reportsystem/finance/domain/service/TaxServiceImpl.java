package com.reportsystem.finance.domain.service;

import com.reportsystem.finance.domain.model.*;
import com.reportsystem.finance.domain.port.inbound.*;
import com.reportsystem.finance.domain.port.outbound.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;


public class TaxServiceImpl implements TaxService {
    private final TaxRecordRepository trRepo;
    private final TaxFilingReportRepository tfrRepo;
    public TaxServiceImpl(TaxRecordRepository trRepo, TaxFilingReportRepository tfrRepo) { this.trRepo = trRepo; this.tfrRepo = tfrRepo; }

    @Override public TaxRecord recordTax(UUID tenantId, UUID branchId, String taxType, int month, int year, BigDecimal taxableAmount, BigDecimal taxRate, String sourceType, UUID sourceId) {
        BigDecimal taxAmount = taxableAmount.multiply(taxRate).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return trRepo.save(TaxRecord.builder().id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId).taxType(taxType)
                .periodMonth(month).periodYear(year).taxableAmount(taxableAmount).taxRate(taxRate).taxAmount(taxAmount)
                .sourceType(sourceType).sourceId(sourceId).createdAt(Instant.now()).build());
    }
    @Override public List<TaxRecord> getTaxRecords(UUID tenantId, String taxType, int year, int month) {
        return trRepo.findByTenantIdAndTaxTypeAndPeriodYearAndPeriodMonth(tenantId, taxType, year, month);
    }
    @Override public TaxFilingReport generateTaxReport(UUID tenantId, String taxType, int year, int month) {
        List<TaxRecord> records = trRepo.findByTenantIdAndTaxTypeAndPeriodYearAndPeriodMonth(tenantId, taxType, year, month);
        BigDecimal totalTax = records.stream().map(TaxRecord::getTaxAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        return tfrRepo.save(TaxFilingReport.builder().id(UUID.randomUUID()).tenantId(tenantId).taxType(taxType)
                .periodMonth(month).periodYear(year).periodType("monthly").totalTax(totalTax).status("draft").createdAt(Instant.now()).build());
    }
}
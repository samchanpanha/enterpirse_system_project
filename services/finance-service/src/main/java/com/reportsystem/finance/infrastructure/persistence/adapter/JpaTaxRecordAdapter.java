package com.reportsystem.finance.infrastructure.persistence.adapter;

import com.reportsystem.finance.domain.model.*;
import com.reportsystem.finance.domain.port.outbound.*;
import com.reportsystem.finance.infrastructure.persistence.entity.*;
import com.reportsystem.finance.infrastructure.persistence.repository.*;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class JpaTaxRecordAdapter implements TaxRecordRepository {
    private final JpaTaxRecordRepository repo;
    public JpaTaxRecordAdapter(JpaTaxRecordRepository r) { repo = r; }
    @Override public TaxRecord save(TaxRecord t) {
        TaxRecordEntity e = new TaxRecordEntity(); e.setId(t.getId()); e.setTenantId(t.getTenantId()); e.setBranchId(t.getBranchId()); e.setTaxType(t.getTaxType());
        e.setPeriodMonth(t.getPeriodMonth()); e.setPeriodYear(t.getPeriodYear()); e.setTaxableAmount(t.getTaxableAmount());
        e.setTaxRate(t.getTaxRate()); e.setTaxAmount(t.getTaxAmount()); e.setSourceType(t.getSourceType()); e.setSourceId(t.getSourceId()); e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e)); }
    @Override public List<TaxRecord> findByTenantIdAndTaxTypeAndPeriodYearAndPeriodMonth(UUID t, String tax, int y, int m) {
        return repo.findByTenantIdAndTaxTypeAndPeriodYearAndPeriodMonth(t, tax, y, m).stream().map(this::toDomain).toList(); }
    private TaxRecord toDomain(TaxRecordEntity e) { return TaxRecord.builder().id(e.getId()).tenantId(e.getTenantId()).taxType(e.getTaxType()).periodMonth(e.getPeriodMonth()).periodYear(e.getPeriodYear()).taxableAmount(e.getTaxableAmount()).taxRate(e.getTaxRate()).taxAmount(e.getTaxAmount()).sourceType(e.getSourceType()).sourceId(e.getSourceId()).createdAt(e.getCreatedAt()).build(); }
}
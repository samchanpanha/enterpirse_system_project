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


public class JpaTaxFilingReportAdapter implements TaxFilingReportRepository {
    private final JpaTaxFilingReportRepository repo;
    public JpaTaxFilingReportAdapter(JpaTaxFilingReportRepository r) { repo = r; }
    @Override public TaxFilingReport save(TaxFilingReport tfr) {
        TaxFilingReportEntity e = new TaxFilingReportEntity(); e.setId(tfr.getId()); e.setTenantId(tfr.getTenantId());
        e.setTaxType(tfr.getTaxType()); e.setPeriodMonth(tfr.getPeriodMonth()); e.setPeriodYear(tfr.getPeriodYear());
        e.setPeriodType(tfr.getPeriodType()); e.setTotalTax(tfr.getTotalTax()); e.setStatus(tfr.getStatus());
        e.setFiledDate(tfr.getFiledDate()); e.setReferenceNumber(tfr.getReferenceNumber()); e.setExportFormat(tfr.getExportFormat());
        e.setExportUrl(tfr.getExportUrl()); e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e)); }
    @Override public Optional<TaxFilingReport> findById(UUID id) { return repo.findById(id).map(this::toDomain); }
    private TaxFilingReport toDomain(TaxFilingReportEntity e) { return TaxFilingReport.builder().id(e.getId()).tenantId(e.getTenantId()).taxType(e.getTaxType()).periodMonth(e.getPeriodMonth()).periodYear(e.getPeriodYear()).periodType(e.getPeriodType()).totalTax(e.getTotalTax()).status(e.getStatus()).filedDate(e.getFiledDate()).referenceNumber(e.getReferenceNumber()).exportFormat(e.getExportFormat()).exportUrl(e.getExportUrl()).createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).build(); }
}
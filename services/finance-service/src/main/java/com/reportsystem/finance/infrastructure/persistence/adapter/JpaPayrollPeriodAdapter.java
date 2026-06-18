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


public class JpaPayrollPeriodAdapter implements PayrollPeriodRepository {
    private final JpaPayrollPeriodRepository repo;
    public JpaPayrollPeriodAdapter(JpaPayrollPeriodRepository r) { repo = r; }
    @Override public PayrollPeriod save(PayrollPeriod p) {
        PayrollPeriodEntity e = new PayrollPeriodEntity(); e.setId(p.getId()); e.setTenantId(p.getTenantId()); e.setBranchId(p.getBranchId());
        e.setPeriodMonth(p.getPeriodMonth()); e.setPeriodYear(p.getPeriodYear()); e.setPeriodType(p.getPeriodType());
        e.setStartDate(p.getStartDate()); e.setEndDate(p.getEndDate()); e.setPaymentDate(p.getPaymentDate());
        e.setStatus(p.getStatus()); e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e)); }
    @Override public Optional<PayrollPeriod> findById(UUID id) { return repo.findById(id).map(this::toDomain); }
    private PayrollPeriod toDomain(PayrollPeriodEntity e) { return PayrollPeriod.builder().id(e.getId()).tenantId(e.getTenantId()).periodMonth(e.getPeriodMonth()).periodYear(e.getPeriodYear()).periodType(e.getPeriodType()).startDate(e.getStartDate()).endDate(e.getEndDate()).paymentDate(e.getPaymentDate()).status(e.getStatus()).createdAt(e.getCreatedAt()).build(); }
}
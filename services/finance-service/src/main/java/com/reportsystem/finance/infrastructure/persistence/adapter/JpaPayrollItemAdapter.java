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


public class JpaPayrollItemAdapter implements PayrollItemRepository {
    private final JpaPayrollItemRepository repo;
    public JpaPayrollItemAdapter(JpaPayrollItemRepository r) { repo = r; }
    @Override public PayrollItem save(PayrollItem pi) {
        PayrollItemEntity e = new PayrollItemEntity(); e.setId(pi.getId()); e.setPayrollPeriodId(pi.getPayrollPeriodId());
        e.setEmployeeId(pi.getEmployeeId()); e.setBaseSalary(pi.getBaseSalary()); e.setAllowances(pi.getAllowances() != null ? pi.getAllowances() : "[]");
        e.setOvertimeAmount(pi.getOvertimeAmount()); e.setGrossSalary(pi.getGrossSalary()); e.setTosAmount(pi.getTosAmount());
        e.setTofbAmount(pi.getTofbAmount()); e.setNssfEmployee(pi.getNssfEmployee()); e.setNssfEmployer(pi.getNssfEmployer());
        e.setDeductions(pi.getDeductions() != null ? pi.getDeductions() : "[]"); e.setNetSalary(pi.getNetSalary()); e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e)); }
    @Override public List<PayrollItem> findByPayrollPeriodId(UUID id) { return repo.findByPayrollPeriodId(id).stream().map(this::toDomain).toList(); }
    private PayrollItem toDomain(PayrollItemEntity e) { return PayrollItem.builder().id(e.getId()).payrollPeriodId(e.getPayrollPeriodId()).employeeId(e.getEmployeeId()).baseSalary(e.getBaseSalary()).allowances(e.getAllowances()).overtimeAmount(e.getOvertimeAmount()).grossSalary(e.getGrossSalary()).tosAmount(e.getTosAmount()).tofbAmount(e.getTofbAmount()).nssfEmployee(e.getNssfEmployee()).nssfEmployer(e.getNssfEmployer()).deductions(e.getDeductions()).netSalary(e.getNetSalary()).createdAt(e.getCreatedAt()).build(); }
}
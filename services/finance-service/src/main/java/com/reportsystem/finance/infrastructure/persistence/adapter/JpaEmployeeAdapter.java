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


public class JpaEmployeeAdapter implements EmployeeRepository {
    private final JpaEmployeeRepository repo;
    public JpaEmployeeAdapter(JpaEmployeeRepository r) { repo = r; }
    @Override public Employee save(Employee e) {
        EmployeeEntity en = new EmployeeEntity(); en.setId(e.getId()); en.setTenantId(e.getTenantId()); en.setBranchId(e.getBranchId()); en.setCode(e.getCode());
        en.setFirstName(e.getFirstName()); en.setLastName(e.getLastName()); en.setKhmerName(e.getKhmerName());
        en.setGender(e.getGender()); en.setBirthDate(e.getBirthDate()); en.setPhone(e.getPhone()); en.setEmail(e.getEmail());
        en.setIdType(e.getIdType()); en.setIdNumber(e.getIdNumber()); en.setPosition(e.getPosition());
        en.setDepartment(e.getDepartment()); en.setHireDate(e.getHireDate()); en.setTerminationDate(e.getTerminationDate());
        en.setStatus(e.getStatus()); en.setBaseSalary(e.getBaseSalary()); en.setBankAccount(e.getBankAccount());
        en.setBankName(e.getBankName()); en.setNssfNumber(e.getNssfNumber()); en.setTaxDependents(e.getTaxDependents());
        en.setCreatedAt(Instant.now());
        return toDomain(repo.save(en)); }
    @Override public Optional<Employee> findById(UUID id) { return repo.findById(id).map(this::toDomain); }
    @Override public List<Employee> findByTenantId(UUID t) { return repo.findByTenantId(t).stream().map(this::toDomain).toList(); }
    @Override public List<Employee> findByTenantIdAndBranchId(UUID t, UUID b) { return repo.findByTenantIdAndBranchId(t, b).stream().map(this::toDomain).toList(); }
    private Employee toDomain(EmployeeEntity e) { return Employee.builder().id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId()).code(e.getCode()).firstName(e.getFirstName()).lastName(e.getLastName()).khmerName(e.getKhmerName()).gender(e.getGender()).birthDate(e.getBirthDate()).phone(e.getPhone()).email(e.getEmail()).idType(e.getIdType()).idNumber(e.getIdNumber()).position(e.getPosition()).department(e.getDepartment()).hireDate(e.getHireDate()).terminationDate(e.getTerminationDate()).status(e.getStatus()).baseSalary(e.getBaseSalary()).bankAccount(e.getBankAccount()).bankName(e.getBankName()).nssfNumber(e.getNssfNumber()).taxDependents(e.getTaxDependents()).createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).build(); }
}
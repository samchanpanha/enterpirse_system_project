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


public class JpaAccountAdapter implements AccountRepository {
    private final JpaAccountRepository repo;
    public JpaAccountAdapter(JpaAccountRepository r) { repo = r; }
    @Override public Account save(Account a) {
        AccountEntity e = new AccountEntity(); e.setId(a.getId()); e.setTenantId(a.getTenantId()); e.setBranchId(a.getBranchId()); e.setCode(a.getCode());
        e.setName(a.getName()); e.setType(a.getType()); e.setActive(a.isActive()); e.setContra(a.isContra());
        e.setParentId(a.getParentId()); e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e)); }
    @Override public Optional<Account> findById(UUID id) { return repo.findById(id).map(this::toDomain); }
    @Override public List<Account> findByTenantId(UUID t) { return repo.findByTenantId(t).stream().map(this::toDomain).toList(); }
    @Override public List<Account> findByTenantIdAndBranchId(UUID t, UUID b) { return repo.findByTenantIdAndBranchId(t, b).stream().map(this::toDomain).toList(); }
    private Account toDomain(AccountEntity e) { return Account.builder().id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId()).code(e.getCode()).name(e.getName()).type(e.getType()).active(e.isActive()).contra(e.isContra()).parentId(e.getParentId()).createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt()).build(); }
}
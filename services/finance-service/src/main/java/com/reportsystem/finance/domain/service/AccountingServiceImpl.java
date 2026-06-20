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


public class AccountingServiceImpl implements AccountingService {
    private final AccountRepository accountRepo;
    private final JournalEntryRepository jeRepo;
    private final JournalEntryLineRepository jelRepo;
    public AccountingServiceImpl(AccountRepository ar, JournalEntryRepository jer, JournalEntryLineRepository jelr) {
        accountRepo = ar; jeRepo = jer; jelRepo = jelr;
    }

    @Override public Account createAccount(UUID tenantId, UUID branchId, String code, String name, String type, UUID parentId) {
        return accountRepo.save(Account.builder().id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId).code(code).name(name).type(type).active(true).parentId(parentId).createdAt(Instant.now()).build());
    }
    @Override public Optional<Account> getAccountById(UUID id) { return accountRepo.findById(id); }
    @Override public List<Account> getAccountsByTenant(UUID tenantId) { return accountRepo.findByTenantId(tenantId); }
    @Override public List<Account> getAccountsByTenantAndBranch(UUID tenantId, UUID branchId) { return accountRepo.findByTenantIdAndBranchId(tenantId, branchId); }
    @Override public Account updateAccount(UUID id, String code, String name, String type, boolean active) {
        Account existing = accountRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Account not found: " + id));
        Account updated = Account.builder().id(existing.getId()).tenantId(existing.getTenantId()).branchId(existing.getBranchId())
                .code(code != null ? code : existing.getCode()).name(name != null ? name : existing.getName())
                .type(type != null ? type : existing.getType()).active(active).contra(existing.isContra())
                .parentId(existing.getParentId()).createdAt(existing.getCreatedAt()).updatedAt(Instant.now()).build();
        return accountRepo.save(updated);
    }

    @Override public JournalEntry postJournalEntry(UUID tenantId, UUID branchId, LocalDate entryDate, String description, String referenceType, UUID referenceId, UUID createdBy, List<JournalEntryLine> lines) {
        BigDecimal totalDebit = lines.stream().map(JournalEntryLine::getDebit).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = lines.stream().map(JournalEntryLine::getCredit).reduce(BigDecimal.ZERO, BigDecimal::add);
        if (totalDebit.compareTo(totalCredit) != 0) throw new IllegalArgumentException("Debit and credit must balance");
        JournalEntry je = jeRepo.save(JournalEntry.builder().id(UUID.randomUUID()).tenantId(tenantId)
                .branchId(branchId)
                .entryNumber(jeRepo.generateEntryNumber(tenantId)).entryDate(entryDate).description(description)
                .referenceType(referenceType).referenceId(referenceId).posted(true).postedAt(Instant.now())
                .createdBy(createdBy).createdAt(Instant.now()).build());
        for (JournalEntryLine line : lines) {
            jelRepo.save(JournalEntryLine.builder().id(UUID.randomUUID()).journalEntryId(je.getId())
                    .accountId(line.getAccountId()).branchId(line.getBranchId() != null ? line.getBranchId() : je.getBranchId())
                    .debit(line.getDebit()).credit(line.getCredit())
                    .description(line.getDescription()).createdAt(Instant.now()).build());
        }
        return je;
    }
    @Override public Optional<JournalEntry> getJournalEntryById(UUID id) { return jeRepo.findById(id); }
    @Override public List<JournalEntry> getJournalEntriesByTenant(UUID tenantId) { return jeRepo.findByTenantId(tenantId); }
}
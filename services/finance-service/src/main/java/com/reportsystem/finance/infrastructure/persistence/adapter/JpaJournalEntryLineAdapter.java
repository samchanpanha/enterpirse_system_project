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


public class JpaJournalEntryLineAdapter implements JournalEntryLineRepository {
    private final JpaJournalEntryLineRepository repo;
    public JpaJournalEntryLineAdapter(JpaJournalEntryLineRepository r) { repo = r; }
    @Override public JournalEntryLine save(JournalEntryLine l) {
        JournalEntryLineEntity e = new JournalEntryLineEntity(); e.setId(l.getId()); e.setJournalEntryId(l.getJournalEntryId());
        e.setAccountId(l.getAccountId()); e.setBranchId(l.getBranchId());
        e.setDebit(l.getDebit()); e.setCredit(l.getCredit()); e.setDescription(l.getDescription()); e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e)); }
    @Override public List<JournalEntryLine> findByJournalEntryId(UUID id) { return repo.findByJournalEntryId(id).stream().map(this::toDomain).toList(); }
    private JournalEntryLine toDomain(JournalEntryLineEntity e) { return JournalEntryLine.builder().id(e.getId()).journalEntryId(e.getJournalEntryId()).accountId(e.getAccountId()).branchId(e.getBranchId()).debit(e.getDebit()).credit(e.getCredit()).description(e.getDescription()).createdAt(e.getCreatedAt()).build(); }
}
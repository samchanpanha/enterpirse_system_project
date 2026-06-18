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


public class JpaJournalEntryAdapter implements JournalEntryRepository {
    private final JpaJournalEntryRepository repo;
    public JpaJournalEntryAdapter(JpaJournalEntryRepository r) { repo = r; }
    @Override public JournalEntry save(JournalEntry je) {
        JournalEntryEntity e = new JournalEntryEntity(); e.setId(je.getId()); e.setTenantId(je.getTenantId());
        e.setBranchId(je.getBranchId());
        e.setFromBranchId(je.getFromBranchId()); e.setToBranchId(je.getToBranchId());
        e.setEntryNumber(je.getEntryNumber()); e.setEntryDate(je.getEntryDate()); e.setDescription(je.getDescription());
        e.setReferenceType(je.getReferenceType()); e.setReferenceId(je.getReferenceId()); e.setPosted(je.isPosted());
        e.setPostedAt(je.getPostedAt()); e.setCreatedBy(je.getCreatedBy()); e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e)); }
    @Override public Optional<JournalEntry> findById(UUID id) { return repo.findById(id).map(this::toDomain); }
    @Override public List<JournalEntry> findByTenantId(UUID t) { return repo.findByTenantId(t).stream().map(this::toDomain).toList(); }
    @Override public String generateEntryNumber(UUID tenantId) { return "JE-" + tenantId.toString().substring(0, 8).toUpperCase() + "-" + System.currentTimeMillis(); }
    private JournalEntry toDomain(JournalEntryEntity e) { return JournalEntry.builder().id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId()).fromBranchId(e.getFromBranchId()).toBranchId(e.getToBranchId()).entryNumber(e.getEntryNumber()).entryDate(e.getEntryDate()).description(e.getDescription()).referenceType(e.getReferenceType()).referenceId(e.getReferenceId()).posted(e.isPosted()).postedAt(e.getPostedAt()).createdBy(e.getCreatedBy()).createdAt(e.getCreatedAt()).build(); }
}
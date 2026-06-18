package com.reportsystem.finance.domain.port.inbound;

import com.reportsystem.finance.domain.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountingService {
    Account createAccount(UUID tenantId, UUID branchId, String code, String name, String type, UUID parentId);
    Optional<Account> getAccountById(UUID id);
    List<Account> getAccountsByTenant(UUID tenantId);
    List<Account> getAccountsByTenantAndBranch(UUID tenantId, UUID branchId);
    JournalEntry postJournalEntry(UUID tenantId, UUID branchId, LocalDate entryDate, String description, String referenceType, UUID referenceId, UUID createdBy, List<JournalEntryLine> lines);
    Optional<JournalEntry> getJournalEntryById(UUID id);
    List<JournalEntry> getJournalEntriesByTenant(UUID tenantId);
}

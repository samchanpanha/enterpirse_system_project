package com.reportsystem.finance.domain.port.outbound;

import com.reportsystem.finance.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JournalEntryRepository {
    JournalEntry save(JournalEntry je);
    Optional<JournalEntry> findById(UUID id);
    List<JournalEntry> findByTenantId(UUID tenantId);
    String generateEntryNumber(UUID tenantId);
}

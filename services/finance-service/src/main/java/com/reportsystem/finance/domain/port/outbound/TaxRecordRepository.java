package com.reportsystem.finance.domain.port.outbound;

import com.reportsystem.finance.domain.model.*;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaxRecordRepository {
    TaxRecord save(TaxRecord t);
    List<TaxRecord> findByTenantIdAndTaxTypeAndPeriodYearAndPeriodMonth(UUID tenantId, String taxType, int year, int month);
}

package com.reportsystem.finance.domain.port.inbound;

import com.reportsystem.finance.domain.model.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaxService {
    TaxRecord recordTax(UUID tenantId, UUID branchId, String taxType, int month, int year, BigDecimal taxableAmount, BigDecimal taxRate, String sourceType, UUID sourceId);
    List<TaxRecord> getTaxRecords(UUID tenantId, String taxType, int year, int month);
    TaxFilingReport generateTaxReport(UUID tenantId, String taxType, int year, int month);
}

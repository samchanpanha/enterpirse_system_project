package com.reportsystem.finance.infrastructure.persistence.repository;

import com.reportsystem.finance.infrastructure.persistence.entity.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaTaxRecordRepository extends JpaRepository<TaxRecordEntity, UUID> { List<TaxRecordEntity> findByTenantIdAndTaxTypeAndPeriodYearAndPeriodMonth(UUID t, String tax, int y, int m); }

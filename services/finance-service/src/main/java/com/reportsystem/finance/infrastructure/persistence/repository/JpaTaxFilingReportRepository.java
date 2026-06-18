package com.reportsystem.finance.infrastructure.persistence.repository;

import com.reportsystem.finance.infrastructure.persistence.entity.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaTaxFilingReportRepository extends JpaRepository<TaxFilingReportEntity, UUID> {}

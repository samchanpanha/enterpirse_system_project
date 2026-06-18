package com.reportsystem.reporting.infrastructure.persistence.repository;

import com.reportsystem.reporting.infrastructure.persistence.entity.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaReportDefinitionRepository extends JpaRepository<ReportDefinitionEntity, UUID> { List<ReportDefinitionEntity> findByTenantId(UUID t); List<ReportDefinitionEntity> findByTenantIdAndBranchId(UUID t, UUID b); }

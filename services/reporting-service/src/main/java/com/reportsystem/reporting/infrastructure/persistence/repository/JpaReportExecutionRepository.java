package com.reportsystem.reporting.infrastructure.persistence.repository;

import com.reportsystem.reporting.infrastructure.persistence.entity.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaReportExecutionRepository extends JpaRepository<ReportExecutionEntity, UUID> { List<ReportExecutionEntity> findByTenantId(UUID t); List<ReportExecutionEntity> findByReportId(UUID reportId); List<ReportExecutionEntity> findByReportIdAndBranchId(UUID reportId, UUID branchId); }

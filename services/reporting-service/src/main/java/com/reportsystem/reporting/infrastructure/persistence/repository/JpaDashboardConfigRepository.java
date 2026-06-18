package com.reportsystem.reporting.infrastructure.persistence.repository;

import com.reportsystem.reporting.infrastructure.persistence.entity.*;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaDashboardConfigRepository extends JpaRepository<DashboardConfigEntity, UUID> { List<DashboardConfigEntity> findByTenantId(UUID t); List<DashboardConfigEntity> findByTenantIdAndBranchId(UUID t, UUID b); }

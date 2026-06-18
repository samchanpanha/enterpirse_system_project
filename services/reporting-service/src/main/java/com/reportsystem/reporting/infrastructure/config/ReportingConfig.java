package com.reportsystem.reporting.infrastructure.config;

import com.reportsystem.reporting.domain.port.outbound.*;
import com.reportsystem.reporting.domain.service.*;
import com.reportsystem.reporting.infrastructure.persistence.repository.*;
import com.reportsystem.reporting.infrastructure.persistence.adapter.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReportingConfig {

    private final JpaReportDefinitionRepository defRepo;
    private final JpaReportExecutionRepository execRepo;
    private final JpaDashboardConfigRepository dashRepo;

    public ReportingConfig(JpaReportDefinitionRepository dr, JpaReportExecutionRepository er, JpaDashboardConfigRepository dcr) {
        defRepo = dr; execRepo = er; dashRepo = dcr;
    }

    @Bean public ReportDefinitionRepository reportDefinitionRepository() { return new JpaReportDefinitionAdapter(defRepo); }
    @Bean public ReportExecutionRepository reportExecutionRepository() { return new JpaReportExecutionAdapter(execRepo); }
    @Bean public DashboardConfigRepository dashboardConfigRepository() { return new JpaDashboardConfigAdapter(dashRepo); }

    @Bean public ReportServiceImpl reportService(ReportDefinitionRepository dr, ReportExecutionRepository er) { return new ReportServiceImpl(dr, er); }
    @Bean public DashboardServiceImpl dashboardService(DashboardConfigRepository dcr) { return new DashboardServiceImpl(dcr); }
}

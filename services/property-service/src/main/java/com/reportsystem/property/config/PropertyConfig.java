package com.reportsystem.property.config;

import com.reportsystem.property.domain.port.outbound.LeaseRepository;
import com.reportsystem.property.domain.port.outbound.MaintenanceRepository;
import com.reportsystem.property.domain.port.outbound.PropertyRepository;
import com.reportsystem.property.domain.port.outbound.ScheduleRepository;
import com.reportsystem.property.domain.port.outbound.UnitRepository;
import com.reportsystem.property.domain.service.LeaseService;
import com.reportsystem.property.domain.service.MaintenanceService;
import com.reportsystem.property.domain.service.PropertyService;
import com.reportsystem.property.domain.service.ScheduleService;
import com.reportsystem.property.domain.service.UnitService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PropertyConfig {

    @Bean
    public PropertyService propertyService(PropertyRepository propertyRepository) {
        return new PropertyService(propertyRepository);
    }

    @Bean
    public UnitService unitService(UnitRepository unitRepository) {
        return new UnitService(unitRepository);
    }

    @Bean
    public LeaseService leaseService(LeaseRepository leaseRepository) {
        return new LeaseService(leaseRepository);
    }

    @Bean
    public ScheduleService scheduleService(ScheduleRepository scheduleRepository, UnitRepository unitRepository) {
        return new ScheduleService(scheduleRepository, unitRepository);
    }

    @Bean
    public MaintenanceService maintenanceService(MaintenanceRepository maintenanceRepository, UnitRepository unitRepository) {
        return new MaintenanceService(maintenanceRepository, unitRepository);
    }
}

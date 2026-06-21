package com.reportsystem.delivery.infrastructure.config;

import com.reportsystem.delivery.domain.port.outbound.*;
import com.reportsystem.delivery.domain.service.*;
import com.reportsystem.delivery.infrastructure.persistence.repository.*;
import com.reportsystem.delivery.infrastructure.persistence.adapter.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DeliveryConfig {

    private final JpaDeliveryRepository deliveryRepo;
    private final JpaDriverRepository driverRepo;
    private final JpaFleetVehicleRepository fleetVehicleRepo;
    private final JpaDeliveryZoneRepository zoneRepo;
    private final JpaDeliveryProofRepository deliveryProofRepo;
    private final JpaDriverPayoutRepository payoutRepo;

    public DeliveryConfig(JpaDeliveryRepository deliveryRepo, JpaDriverRepository driverRepo,
                          JpaFleetVehicleRepository fleetVehicleRepo, JpaDeliveryZoneRepository zoneRepo,
                          JpaDeliveryProofRepository deliveryProofRepo, JpaDriverPayoutRepository payoutRepo) {
        this.deliveryRepo = deliveryRepo; this.driverRepo = driverRepo;
        this.fleetVehicleRepo = fleetVehicleRepo; this.zoneRepo = zoneRepo;
        this.deliveryProofRepo = deliveryProofRepo; this.payoutRepo = payoutRepo;
    }

    @Bean
    public DeliveryRepository deliveryRepository() { return new JpaDeliveryAdapter(deliveryRepo); }

    @Bean
    public DriverRepository driverRepository() { return new JpaDriverAdapter(driverRepo); }

    @Bean
    public FleetVehicleRepository fleetVehicleRepository() { return new JpaFleetVehicleAdapter(fleetVehicleRepo); }

    @Bean
    public DeliveryZoneRepository deliveryZoneRepository() { return new JpaDeliveryZoneAdapter(zoneRepo); }

    @Bean
    public DeliveryProofRepository deliveryProofRepository() { return new JpaDeliveryProofAdapter(deliveryProofRepo); }

    @Bean
    public DriverPayoutRepository driverPayoutRepository() { return new JpaDriverPayoutAdapter(payoutRepo); }

    @Bean
    public DeliveryServiceImpl deliveryService(DeliveryRepository dRepo) {
        return new DeliveryServiceImpl(dRepo);
    }

    @Bean
    public DriverServiceImpl driverService(DriverRepository dRepo) {
        return new DriverServiceImpl(dRepo);
    }

    @Bean
    public FleetServiceImpl fleetService(FleetVehicleRepository fRepo) {
        return new FleetServiceImpl(fRepo);
    }

    @Bean
    public ZoneServiceImpl zoneService(DeliveryZoneRepository zRepo) {
        return new ZoneServiceImpl(zRepo);
    }

    @Bean
    public PayoutServiceImpl payoutService(DriverPayoutRepository pRepo) {
        return new PayoutServiceImpl(pRepo);
    }
}

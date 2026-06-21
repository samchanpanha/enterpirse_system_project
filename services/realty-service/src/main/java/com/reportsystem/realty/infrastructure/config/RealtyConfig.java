package com.reportsystem.realty.infrastructure.config;

import com.reportsystem.realty.domain.port.outbound.*;
import com.reportsystem.realty.domain.service.*;
import com.reportsystem.realty.infrastructure.persistence.repository.*;
import com.reportsystem.realty.infrastructure.persistence.adapter.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RealtyConfig {

    private final JpaListingRepository listingRepo;
    private final JpaListingImageRepository listingImageRepo;
    private final JpaAgentRepository agentRepo;
    private final JpaLeadRepository leadRepo;
    private final JpaTourRepository tourRepo;
    private final JpaOfferRepository offerRepo;
    private final JpaCommissionRepository commissionRepo;
    private final JpaPropertyValuationRepository valuationRepo;
    private final JpaHoaFeeRepository hoaFeeRepo;
    private final JpaResidentRepository residentRepo;
    private final JpaAmenityBookingRepository amenityBookingRepo;
    private final JpaVisitorRepository visitorRepo;
    private final JpaParcelRepository parcelRepo;
    private final JpaCustomerRepository customerRepo;

    public RealtyConfig(JpaListingRepository listingRepo, JpaListingImageRepository listingImageRepo,
                        JpaAgentRepository agentRepo, JpaLeadRepository leadRepo,
                        JpaTourRepository tourRepo, JpaOfferRepository offerRepo,
                        JpaCommissionRepository commissionRepo, JpaPropertyValuationRepository valuationRepo,
                        JpaHoaFeeRepository hoaFeeRepo, JpaResidentRepository residentRepo,
                        JpaAmenityBookingRepository amenityBookingRepo, JpaVisitorRepository visitorRepo,
                        JpaParcelRepository parcelRepo, JpaCustomerRepository customerRepo) {
        this.listingRepo = listingRepo; this.listingImageRepo = listingImageRepo;
        this.agentRepo = agentRepo; this.leadRepo = leadRepo;
        this.tourRepo = tourRepo; this.offerRepo = offerRepo;
        this.commissionRepo = commissionRepo; this.valuationRepo = valuationRepo;
        this.hoaFeeRepo = hoaFeeRepo; this.residentRepo = residentRepo;
        this.amenityBookingRepo = amenityBookingRepo; this.visitorRepo = visitorRepo;
        this.parcelRepo = parcelRepo; this.customerRepo = customerRepo;
    }

    @Bean
    public ListingRepository listingRepository() { return new JpaListingAdapter(listingRepo); }

    @Bean
    public ListingImageRepository listingImageRepository() { return new JpaListingImageAdapter(listingImageRepo); }

    @Bean
    public AgentRepository agentRepository() { return new JpaAgentAdapter(agentRepo); }

    @Bean
    public LeadRepository leadRepository() { return new JpaLeadAdapter(leadRepo); }

    @Bean
    public TourRepository tourRepository() { return new JpaTourAdapter(tourRepo); }

    @Bean
    public OfferRepository offerRepository() { return new JpaOfferAdapter(offerRepo); }

    @Bean
    public CommissionRepository commissionRepository() { return new JpaCommissionAdapter(commissionRepo); }

    @Bean
    public PropertyValuationRepository propertyValuationRepository() { return new JpaPropertyValuationAdapter(valuationRepo); }

    @Bean
    public HoaFeeRepository hoaFeeRepository() { return new JpaHoaFeeAdapter(hoaFeeRepo); }

    @Bean
    public ResidentRepository residentRepository() { return new JpaResidentAdapter(residentRepo); }

    @Bean
    public AmenityBookingRepository amenityBookingRepository() { return new JpaAmenityBookingAdapter(amenityBookingRepo); }

    @Bean
    public VisitorRepository visitorRepository() { return new JpaVisitorAdapter(visitorRepo); }

    @Bean
    public ParcelRepository parcelRepository() { return new JpaParcelAdapter(parcelRepo); }

    @Bean
    public CustomerRepository customerRepository() { return new JpaCustomerAdapter(customerRepo); }

    @Bean
    public ListingServiceImpl listingService(ListingRepository lRepo) { return new ListingServiceImpl(lRepo); }

    @Bean
    public AgentServiceImpl agentService(AgentRepository aRepo) { return new AgentServiceImpl(aRepo); }

    @Bean
    public LeadServiceImpl leadService(LeadRepository lRepo) { return new LeadServiceImpl(lRepo); }

    @Bean
    public TourServiceImpl tourService(TourRepository tRepo) { return new TourServiceImpl(tRepo); }

    @Bean
    public OfferServiceImpl offerService(OfferRepository oRepo) { return new OfferServiceImpl(oRepo); }

    @Bean
    public ValuationServiceImpl valuationService(PropertyValuationRepository vRepo) { return new ValuationServiceImpl(vRepo); }

    @Bean
    public HoaServiceImpl hoaService(HoaFeeRepository hRepo) { return new HoaServiceImpl(hRepo); }

    @Bean
    public ResidentServiceImpl residentService(ResidentRepository rRepo) { return new ResidentServiceImpl(rRepo); }

    @Bean
    public AmenityServiceImpl amenityService(AmenityBookingRepository aRepo) { return new AmenityServiceImpl(aRepo); }

    @Bean
    public VisitorServiceImpl visitorService(VisitorRepository vRepo) { return new VisitorServiceImpl(vRepo); }

    @Bean
    public ParcelServiceImpl parcelService(ParcelRepository pRepo) { return new ParcelServiceImpl(pRepo); }

    @Bean
    public CustomerServiceImpl customerService(CustomerRepository cRepo) { return new CustomerServiceImpl(cRepo); }
}

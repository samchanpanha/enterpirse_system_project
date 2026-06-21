package com.reportsystem.realty.domain.service;

import com.reportsystem.realty.domain.model.HoaFee;
import com.reportsystem.realty.domain.port.inbound.HoaService;
import com.reportsystem.realty.domain.port.outbound.HoaFeeRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class HoaServiceImpl implements HoaService {

    private final HoaFeeRepository hoaFeeRepository;

    public HoaServiceImpl(HoaFeeRepository hoaFeeRepository) {
        this.hoaFeeRepository = hoaFeeRepository;
    }

    @Override
    public HoaFee createHoaFee(UUID tenantId, UUID branchId, UUID propertyId, UUID residentId, BigDecimal amount, LocalDate dueDate) {
        HoaFee hoaFee = HoaFee.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId)
                .propertyId(propertyId).residentId(residentId)
                .amount(amount).dueDate(dueDate).status("pending")
                .lateFee(BigDecimal.ZERO)
                .createdAt(Instant.now()).build();
        return hoaFeeRepository.save(hoaFee);
    }

    @Override
    public Optional<HoaFee> getHoaFeeById(UUID id) {
        return hoaFeeRepository.findById(id);
    }

    @Override
    public List<HoaFee> getHoaFeesByTenant(UUID tenantId) {
        return hoaFeeRepository.findByTenantId(tenantId);
    }

    @Override
    public List<HoaFee> getHoaFeesByTenantAndBranch(UUID tenantId, UUID branchId) {
        return hoaFeeRepository.findByTenantIdAndBranchId(tenantId, branchId);
    }

    @Override
    public List<HoaFee> getHoaFeesByProperty(UUID propertyId) {
        return hoaFeeRepository.findByPropertyId(propertyId);
    }

    @Override
    public HoaFee updateHoaFee(HoaFee hoaFee) {
        return hoaFeeRepository.save(hoaFee);
    }
}

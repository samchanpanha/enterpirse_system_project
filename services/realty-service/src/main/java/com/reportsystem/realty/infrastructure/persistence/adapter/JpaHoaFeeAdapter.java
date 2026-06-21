package com.reportsystem.realty.infrastructure.persistence.adapter;

import com.reportsystem.realty.domain.model.HoaFee;
import com.reportsystem.realty.domain.port.outbound.HoaFeeRepository;
import com.reportsystem.realty.infrastructure.persistence.entity.HoaFeeEntity;
import com.reportsystem.realty.infrastructure.persistence.repository.JpaHoaFeeRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaHoaFeeAdapter implements HoaFeeRepository {

    private final JpaHoaFeeRepository repo;
    public JpaHoaFeeAdapter(JpaHoaFeeRepository repo) { this.repo = repo; }

    @Override
    public HoaFee save(HoaFee hoaFee) {
        HoaFeeEntity e = new HoaFeeEntity();
        e.setId(hoaFee.getId());
        e.setTenantId(hoaFee.getTenantId());
        e.setBranchId(hoaFee.getBranchId());
        e.setPropertyId(hoaFee.getPropertyId());
        e.setResidentId(hoaFee.getResidentId());
        e.setAmount(hoaFee.getAmount());
        e.setDueDate(hoaFee.getDueDate());
        e.setPeriod(hoaFee.getPeriod());
        e.setStatus(hoaFee.getStatus() != null ? hoaFee.getStatus() : "pending");
        e.setPaidAt(hoaFee.getPaidAt());
        e.setPaymentRef(hoaFee.getPaymentRef());
        e.setLateFee(hoaFee.getLateFee() != null ? hoaFee.getLateFee() : java.math.BigDecimal.ZERO);
        e.setNotes(hoaFee.getNotes());
        e.setCreatedAt(Instant.now());
        return toDomain(repo.save(e));
    }

    @Override
    public Optional<HoaFee> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<HoaFee> findByTenantId(UUID tenantId) {
        return repo.findByTenantId(tenantId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<HoaFee> findByTenantIdAndBranchId(UUID tenantId, UUID branchId) {
        return repo.findByTenantIdAndBranchId(tenantId, branchId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<HoaFee> findByPropertyId(UUID propertyId) {
        return repo.findByPropertyId(propertyId).stream().map(this::toDomain).toList();
    }

    private HoaFee toDomain(HoaFeeEntity e) {
        return HoaFee.builder()
                .id(e.getId()).tenantId(e.getTenantId()).branchId(e.getBranchId())
                .propertyId(e.getPropertyId()).residentId(e.getResidentId())
                .amount(e.getAmount()).dueDate(e.getDueDate()).period(e.getPeriod())
                .status(e.getStatus()).paidAt(e.getPaidAt()).paymentRef(e.getPaymentRef())
                .lateFee(e.getLateFee()).notes(e.getNotes())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }
}

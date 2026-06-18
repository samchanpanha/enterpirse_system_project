package com.reportsystem.property.domain.service;

import com.reportsystem.property.domain.model.Lease;
import com.reportsystem.property.domain.port.inbound.LeaseUseCase;
import com.reportsystem.property.domain.port.outbound.LeaseRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class LeaseService implements LeaseUseCase {

    private final LeaseRepository leaseRepository;

    public LeaseService(LeaseRepository leaseRepository) {
        this.leaseRepository = leaseRepository;
    }

    @Override
    public Lease createLease(UUID tenantId, UUID branchId, UUID unitId, String tenantName, String tenantPhone,
                             LocalDate startDate, LocalDate endDate,
                             BigDecimal rentAmount, BigDecimal depositAmount) {
        if (leaseRepository.hasOverlappingLease(unitId, startDate, endDate)) {
            throw new IllegalArgumentException("Overlapping lease exists for unit: " + unitId);
        }
        Lease lease = Lease.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).branchId(branchId).unitId(unitId)
                .tenantName(tenantName).tenantPhone(tenantPhone)
                .startDate(startDate).endDate(endDate)
                .rentAmount(rentAmount).depositAmount(depositAmount)
                .paymentDay(1).status("active").documents("[]")
                .createdAt(Instant.now())
                .build();
        return leaseRepository.save(lease);
    }

    @Override
    public Optional<Lease> getLeaseById(UUID id) {
        return leaseRepository.findById(id);
    }

    @Override
    public List<Lease> getLeasesByUnit(UUID unitId) {
        return leaseRepository.findByUnitId(unitId);
    }

    @Override
    public List<Lease> getActiveLeasesByTenant(UUID tenantId) {
        return leaseRepository.findActiveByTenantId(tenantId);
    }

    @Override
    public Lease terminateLease(UUID id) {
        Lease lease = leaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lease not found: " + id));
        Lease updated = Lease.builder()
                .id(lease.getId()).tenantId(lease.getTenantId()).unitId(lease.getUnitId())
                .tenantName(lease.getTenantName()).tenantPhone(lease.getTenantPhone())
                .tenantEmail(lease.getTenantEmail()).idType(lease.getIdType()).idNumber(lease.getIdNumber())
                .startDate(lease.getStartDate()).endDate(lease.getEndDate())
                .rentAmount(lease.getRentAmount()).depositAmount(lease.getDepositAmount())
                .paymentDay(lease.getPaymentDay()).status("terminated")
                .documents(lease.getDocuments()).notes(lease.getNotes())
                .createdAt(lease.getCreatedAt()).updatedAt(Instant.now())
                .build();
        return leaseRepository.save(updated);
    }

    @Override
    public Lease renewLease(UUID id, LocalDate newEndDate, BigDecimal newRent) {
        Lease lease = leaseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Lease not found: " + id));
        Lease renewed = Lease.builder()
                .id(lease.getId()).tenantId(lease.getTenantId()).unitId(lease.getUnitId())
                .tenantName(lease.getTenantName()).tenantPhone(lease.getTenantPhone())
                .tenantEmail(lease.getTenantEmail()).idType(lease.getIdType()).idNumber(lease.getIdNumber())
                .startDate(lease.getEndDate().plusDays(1)).endDate(newEndDate)
                .rentAmount(newRent != null ? newRent : lease.getRentAmount())
                .depositAmount(lease.getDepositAmount())
                .paymentDay(lease.getPaymentDay()).status("active")
                .documents(lease.getDocuments()).notes(lease.getNotes())
                .createdAt(lease.getCreatedAt()).updatedAt(Instant.now())
                .build();
        return leaseRepository.save(renewed);
    }
}

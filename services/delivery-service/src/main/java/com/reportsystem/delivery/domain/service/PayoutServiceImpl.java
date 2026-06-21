package com.reportsystem.delivery.domain.service;

import com.reportsystem.delivery.domain.model.DriverPayout;
import com.reportsystem.delivery.domain.port.inbound.PayoutService;
import com.reportsystem.delivery.domain.port.outbound.DriverPayoutRepository;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PayoutServiceImpl implements PayoutService {

    private final DriverPayoutRepository payoutRepository;

    public PayoutServiceImpl(DriverPayoutRepository payoutRepository) {
        this.payoutRepository = payoutRepository;
    }

    @Override
    public DriverPayout createPayout(UUID tenantId, UUID driverId, LocalDate periodStart, LocalDate periodEnd) {
        DriverPayout payout = DriverPayout.builder()
                .id(UUID.randomUUID()).tenantId(tenantId).driverId(driverId)
                .periodStart(periodStart).periodEnd(periodEnd)
                .totalDeliveries(0).totalDistance(BigDecimal.ZERO)
                .totalEarnings(BigDecimal.ZERO).commissionAmount(BigDecimal.ZERO)
                .bonusAmount(BigDecimal.ZERO).deductionAmount(BigDecimal.ZERO)
                .netAmount(BigDecimal.ZERO).status("pending")
                .createdAt(Instant.now()).build();
        return payoutRepository.save(payout);
    }

    @Override
    public Optional<DriverPayout> getPayoutById(UUID id) {
        return payoutRepository.findById(id);
    }

    @Override
    public List<DriverPayout> getPayoutsByTenant(UUID tenantId) {
        return payoutRepository.findByTenantId(tenantId);
    }

    @Override
    public List<DriverPayout> getPayoutsByDriver(UUID driverId) {
        return payoutRepository.findByDriverId(driverId);
    }

    @Override
    public DriverPayout updateStatus(UUID id, String status) {
        DriverPayout existing = payoutRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("DriverPayout not found: " + id));
        DriverPayout updated = DriverPayout.builder()
                .id(existing.getId()).tenantId(existing.getTenantId()).driverId(existing.getDriverId())
                .periodStart(existing.getPeriodStart()).periodEnd(existing.getPeriodEnd())
                .totalDeliveries(existing.getTotalDeliveries()).totalDistance(existing.getTotalDistance())
                .totalEarnings(existing.getTotalEarnings()).commissionAmount(existing.getCommissionAmount())
                .bonusAmount(existing.getBonusAmount()).deductionAmount(existing.getDeductionAmount())
                .netAmount(existing.getNetAmount()).status(status)
                .paidAt("paid".equals(status) ? Instant.now() : existing.getPaidAt())
                .createdAt(existing.getCreatedAt()).updatedAt(Instant.now())
                .build();
        return payoutRepository.save(updated);
    }
}

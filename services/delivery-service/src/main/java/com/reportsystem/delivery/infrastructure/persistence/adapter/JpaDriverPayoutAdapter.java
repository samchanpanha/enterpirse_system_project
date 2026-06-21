package com.reportsystem.delivery.infrastructure.persistence.adapter;

import com.reportsystem.delivery.domain.model.DriverPayout;
import com.reportsystem.delivery.domain.port.outbound.DriverPayoutRepository;
import com.reportsystem.delivery.infrastructure.persistence.entity.DriverPayoutEntity;
import com.reportsystem.delivery.infrastructure.persistence.repository.JpaDriverPayoutRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaDriverPayoutAdapter implements DriverPayoutRepository {

    private final JpaDriverPayoutRepository repo;
    public JpaDriverPayoutAdapter(JpaDriverPayoutRepository repo) { this.repo = repo; }

    @Override
    public DriverPayout save(DriverPayout payout) {
        DriverPayoutEntity e = toEntity(payout);
        DriverPayoutEntity saved = repo.save(e);
        return toDomain(saved);
    }

    @Override
    public Optional<DriverPayout> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<DriverPayout> findByTenantId(UUID tenantId) {
        return repo.findByTenantId(tenantId).stream().map(this::toDomain).toList();
    }

    @Override
    public List<DriverPayout> findByDriverId(UUID driverId) {
        return repo.findByDriverId(driverId).stream().map(this::toDomain).toList();
    }

    private DriverPayoutEntity toEntity(DriverPayout p) {
        DriverPayoutEntity e = new DriverPayoutEntity();
        e.setId(p.getId());
        e.setTenantId(p.getTenantId());
        e.setDriverId(p.getDriverId());
        e.setPeriodStart(p.getPeriodStart());
        e.setPeriodEnd(p.getPeriodEnd());
        e.setTotalDeliveries(p.getTotalDeliveries());
        e.setTotalDistance(p.getTotalDistance());
        e.setTotalEarnings(p.getTotalEarnings());
        e.setCommissionAmount(p.getCommissionAmount());
        e.setBonusAmount(p.getBonusAmount());
        e.setDeductionAmount(p.getDeductionAmount());
        e.setNetAmount(p.getNetAmount());
        e.setStatus(p.getStatus());
        e.setPaidAt(p.getPaidAt());
        e.setCreatedAt(p.getCreatedAt());
        e.setUpdatedAt(Instant.now());
        return e;
    }

    private DriverPayout toDomain(DriverPayoutEntity e) {
        return DriverPayout.builder()
                .id(e.getId()).tenantId(e.getTenantId()).driverId(e.getDriverId())
                .periodStart(e.getPeriodStart()).periodEnd(e.getPeriodEnd())
                .totalDeliveries(e.getTotalDeliveries()).totalDistance(e.getTotalDistance())
                .totalEarnings(e.getTotalEarnings()).commissionAmount(e.getCommissionAmount())
                .bonusAmount(e.getBonusAmount()).deductionAmount(e.getDeductionAmount())
                .netAmount(e.getNetAmount()).status(e.getStatus()).paidAt(e.getPaidAt())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }
}

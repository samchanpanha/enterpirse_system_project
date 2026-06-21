package com.reportsystem.payment.infrastructure.persistence.adapter;

import com.reportsystem.payment.domain.model.PaymentGatewayConfig;
import com.reportsystem.payment.domain.port.outbound.PaymentGatewayConfigRepository;
import com.reportsystem.payment.infrastructure.persistence.entity.PaymentGatewayConfigEntity;
import com.reportsystem.payment.infrastructure.persistence.repository.JpaPaymentGatewayConfigRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JpaPaymentGatewayConfigAdapter implements PaymentGatewayConfigRepository {

    private final JpaPaymentGatewayConfigRepository repo;
    public JpaPaymentGatewayConfigAdapter(JpaPaymentGatewayConfigRepository repo) { this.repo = repo; }

    @Override
    public PaymentGatewayConfig save(PaymentGatewayConfig config) {
        PaymentGatewayConfigEntity e = toEntity(config);
        return toDomain(repo.save(e));
    }

    @Override
    public Optional<PaymentGatewayConfig> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<PaymentGatewayConfig> findByTenantId(UUID tenantId) {
        return repo.findByTenantId(tenantId).stream().map(this::toDomain).toList();
    }

    @Override
    public void deleteById(UUID id) {
        repo.deleteById(id);
    }

    private PaymentGatewayConfig toDomain(PaymentGatewayConfigEntity e) {
        return PaymentGatewayConfig.builder()
                .id(e.getId()).tenantId(e.getTenantId())
                .gateway(e.getGateway()).config(e.getConfig())
                .active(e.isActive()).sandbox(e.isSandbox())
                .createdAt(e.getCreatedAt()).updatedAt(e.getUpdatedAt())
                .build();
    }

    private PaymentGatewayConfigEntity toEntity(PaymentGatewayConfig config) {
        PaymentGatewayConfigEntity e = new PaymentGatewayConfigEntity();
        e.setId(config.getId());
        e.setTenantId(config.getTenantId());
        e.setGateway(config.getGateway());
        e.setConfig(config.getConfig());
        e.setActive(config.isActive());
        e.setSandbox(config.isSandbox());
        e.setCreatedAt(config.getCreatedAt() != null ? config.getCreatedAt() : Instant.now());
        e.setUpdatedAt(config.getUpdatedAt());
        return e;
    }
}

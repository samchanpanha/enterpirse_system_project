package com.reportsystem.payment.domain.service;

import com.reportsystem.payment.domain.model.PaymentGatewayConfig;
import com.reportsystem.payment.domain.port.inbound.PaymentGatewayConfigService;
import com.reportsystem.payment.domain.port.outbound.PaymentGatewayConfigRepository;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PaymentGatewayConfigServiceImpl implements PaymentGatewayConfigService {

    private final PaymentGatewayConfigRepository repository;

    public PaymentGatewayConfigServiceImpl(PaymentGatewayConfigRepository repository) {
        this.repository = repository;
    }

    @Override
    public PaymentGatewayConfig create(PaymentGatewayConfig config) {
        return repository.save(config);
    }

    @Override
    public PaymentGatewayConfig update(UUID id, PaymentGatewayConfig config) {
        PaymentGatewayConfig existing = repository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Gateway config not found: " + id));
        PaymentGatewayConfig updated = PaymentGatewayConfig.builder()
                .id(existing.getId()).tenantId(existing.getTenantId())
                .gateway(config.getGateway() != null ? config.getGateway() : existing.getGateway())
                .config(config.getConfig() != null ? config.getConfig() : existing.getConfig())
                .active(config.isActive())
                .sandbox(config.isSandbox())
                .createdAt(existing.getCreatedAt()).updatedAt(Instant.now())
                .build();
        return repository.save(updated);
    }

    @Override
    public Optional<PaymentGatewayConfig> getById(UUID id) {
        return repository.findById(id);
    }

    @Override
    public List<PaymentGatewayConfig> getByTenant(UUID tenantId) {
        return repository.findByTenantId(tenantId);
    }

    @Override
    public void delete(UUID id) {
        repository.deleteById(id);
    }
}

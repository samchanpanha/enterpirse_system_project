package com.reportsystem.payment.domain.port.outbound;

import com.reportsystem.payment.domain.model.PaymentGatewayConfig;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentGatewayConfigRepository {
    PaymentGatewayConfig save(PaymentGatewayConfig config);
    Optional<PaymentGatewayConfig> findById(UUID id);
    List<PaymentGatewayConfig> findByTenantId(UUID tenantId);
    void deleteById(UUID id);
}

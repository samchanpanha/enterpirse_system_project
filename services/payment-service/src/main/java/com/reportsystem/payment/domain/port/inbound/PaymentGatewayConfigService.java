package com.reportsystem.payment.domain.port.inbound;

import com.reportsystem.payment.domain.model.PaymentGatewayConfig;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentGatewayConfigService {
    PaymentGatewayConfig create(PaymentGatewayConfig config);
    PaymentGatewayConfig update(UUID id, PaymentGatewayConfig config);
    Optional<PaymentGatewayConfig> getById(UUID id);
    List<PaymentGatewayConfig> getByTenant(UUID tenantId);
    void delete(UUID id);
}

package com.reportsystem.payment.infrastructure.persistence.repository;

import com.reportsystem.payment.infrastructure.persistence.entity.PaymentGatewayConfigEntity;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JpaPaymentGatewayConfigRepository extends JpaRepository<PaymentGatewayConfigEntity, UUID> {
    List<PaymentGatewayConfigEntity> findByTenantId(UUID tenantId);
}

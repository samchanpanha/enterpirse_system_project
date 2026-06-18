package com.reportsystem.payment.infrastructure.gateway;

import com.reportsystem.payment.domain.model.Transaction;
import com.reportsystem.payment.domain.port.outbound.PaymentGatewayPort;
import java.math.BigDecimal;

public class CashAdapter implements PaymentGatewayPort {
    @Override public GatewayResult process(Transaction transaction) {
        return new GatewayResult(true, "CASH-" + transaction.getTransactionId(), "{\"status\":\"completed\",\"method\":\"cash\"}", null);
    }
    @Override public GatewayResult refund(String gatewayRef, BigDecimal amount) {
        return new GatewayResult(true, "REF-" + gatewayRef, "{\"status\":\"refunded\",\"method\":\"cash\"}", null);
    }
}

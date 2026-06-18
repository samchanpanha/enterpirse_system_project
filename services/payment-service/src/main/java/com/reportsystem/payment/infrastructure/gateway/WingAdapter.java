package com.reportsystem.payment.infrastructure.gateway;

import com.reportsystem.payment.domain.model.Transaction;
import com.reportsystem.payment.domain.port.outbound.PaymentGatewayPort;
import java.math.BigDecimal;

public class WingAdapter implements PaymentGatewayPort {
    public WingAdapter(String apiUrl, String merchantId, String apiKey) {}
    @Override public GatewayResult process(Transaction transaction) {
        return new GatewayResult(true, "WING-" + transaction.getTransactionId(), "{\"status\":\"approved\"}", null);
    }
    @Override public GatewayResult refund(String gatewayRef, BigDecimal amount) {
        return new GatewayResult(true, "REF-" + gatewayRef, "{\"status\":\"refunded\"}", null);
    }
}

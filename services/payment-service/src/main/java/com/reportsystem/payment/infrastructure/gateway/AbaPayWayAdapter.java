package com.reportsystem.payment.infrastructure.gateway;

import com.reportsystem.payment.domain.model.Transaction;
import com.reportsystem.payment.domain.port.outbound.PaymentGatewayPort;
import java.math.BigDecimal;

public class AbaPayWayAdapter implements PaymentGatewayPort {
    private final String apiUrl;
    private final String merchantId;
    private final String apiKey;
    public AbaPayWayAdapter(String apiUrl, String merchantId, String apiKey) { this.apiUrl = apiUrl; this.merchantId = merchantId; this.apiKey = apiKey; }
    @Override public GatewayResult process(Transaction transaction) {
        // Stub: real integration would call ABA PayWay REST API
        return new GatewayResult(true, "ABA-" + transaction.getTransactionId(), "{\"status\":\"approved\"}", null);
    }
    @Override public GatewayResult refund(String gatewayRef, BigDecimal amount) {
        return new GatewayResult(true, "REF-" + gatewayRef, "{\"status\":\"refunded\"}", null);
    }
}

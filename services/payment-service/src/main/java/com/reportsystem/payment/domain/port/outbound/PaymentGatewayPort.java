package com.reportsystem.payment.domain.port.outbound;

import com.reportsystem.payment.domain.model.Transaction;
import java.math.BigDecimal;

public interface PaymentGatewayPort {
    GatewayResult process(Transaction transaction);
    GatewayResult refund(String gatewayRef, BigDecimal amount);
    record GatewayResult(boolean success, String gatewayRef, String rawResponse, String errorMessage) {}
}

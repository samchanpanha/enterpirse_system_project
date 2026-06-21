package com.reportsystem.payment.infrastructure.config;

import com.reportsystem.payment.domain.port.inbound.PaymentGatewayConfigService;
import com.reportsystem.payment.domain.port.outbound.*;
import com.reportsystem.payment.domain.service.*;
import com.reportsystem.payment.infrastructure.gateway.*;
import com.reportsystem.payment.infrastructure.persistence.repository.*;
import com.reportsystem.payment.infrastructure.persistence.adapter.*;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PaymentConfig {

    private final JpaTransactionRepository txRepo;
    private final JpaGatewayLogRepository logRepo;
    private final JpaRefundRepository refundRepo;
    private final JpaReconciliationRecordRepository recRepo;
    private final JpaPaymentGatewayConfigRepository gwConfigRepo;

    public PaymentConfig(JpaTransactionRepository txr, JpaGatewayLogRepository lr,
                         JpaRefundRepository rfr, JpaReconciliationRecordRepository rcr,
                         JpaPaymentGatewayConfigRepository gcr) {
        txRepo = txr; logRepo = lr; refundRepo = rfr; recRepo = rcr; gwConfigRepo = gcr;
    }

    @Bean public TransactionRepository transactionRepository() { return new JpaTransactionAdapter(txRepo); }
    @Bean public GatewayLogRepository gatewayLogRepository() { return new JpaGatewayLogAdapter(logRepo); }
    @Bean public RefundRepository refundRepository() { return new JpaRefundAdapter(refundRepo); }
    @Bean public ReconciliationRecordRepository reconciliationRecordRepository() { return new JpaReconciliationRecordAdapter(recRepo); }

    @Bean public PaymentGatewayRouter paymentGatewayRouter(
            @Value("${payment.gateways.aba.api-url}") String abaUrl,
            @Value("${payment.gateways.aba.merchant-id}") String abaMerchant,
            @Value("${payment.gateways.aba.api-key}") String abaKey,
            @Value("${payment.gateways.wing.api-url}") String wingUrl,
            @Value("${payment.gateways.wing.merchant-id}") String wingMerchant,
            @Value("${payment.gateways.wing.api-key}") String wingKey,
            @Value("${payment.gateways.pipay.api-url}") String piUrl,
            @Value("${payment.gateways.pipay.merchant-id}") String piMerchant,
            @Value("${payment.gateways.pipay.api-key}") String piKey) {
        return new PaymentGatewayRouter(Map.of(
                "aba", new AbaPayWayAdapter(abaUrl, abaMerchant, abaKey),
                "wing", new WingAdapter(wingUrl, wingMerchant, wingKey),
                "pipay", new PiPayAdapter(piUrl, piMerchant, piKey),
                "cash", new CashAdapter()
        ));
    }

    @Bean public PaymentServiceImpl paymentService(TransactionRepository txr, RefundRepository rfr, GatewayLogRepository lgr, PaymentGatewayRouter r) { return new PaymentServiceImpl(txr, rfr, lgr, r); }
    @Bean public ReconciliationServiceImpl reconciliationService(ReconciliationRecordRepository rr) { return new ReconciliationServiceImpl(rr); }
    @Bean public PaymentGatewayConfigRepository paymentGatewayConfigRepository() { return new JpaPaymentGatewayConfigAdapter(gwConfigRepo); }
    @Bean public PaymentGatewayConfigService paymentGatewayConfigService(PaymentGatewayConfigRepository r) { return new PaymentGatewayConfigServiceImpl(r); }
}

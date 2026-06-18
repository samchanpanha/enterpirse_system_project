package com.reportsystem.payment.infrastructure.messaging;

import com.reportsystem.shared.dto.event.PaymentEvent;
import com.reportsystem.payment.domain.model.Transaction;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventPublisher {

    private final KafkaTemplate<String, Object> kafka;

    public PaymentEventPublisher(KafkaTemplate<String, Object> kafka) {
        this.kafka = kafka;
    }

    public void publishPaymentReceived(Transaction tx) {
        kafka.send("payment.received", tx.getTenantId().toString(),
                new PaymentEvent(tx.getId(), tx.getTenantId(), tx.getInvoiceId(), tx.getAmount(),
                        tx.getGateway(), "completed", tx.getPaidAt()));
    }

    public void publishPaymentFailed(Transaction tx) {
        kafka.send("payment.failed", tx.getTenantId().toString(),
                new PaymentEvent(tx.getId(), tx.getTenantId(), tx.getInvoiceId(), tx.getAmount(),
                        tx.getGateway(), "failed", null));
    }
}

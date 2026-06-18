package com.reportsystem.property.infrastructure.event;

import com.reportsystem.shared.dto.event.RentInvoiceEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class RentInvoiceEventPublisher {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public RentInvoiceEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(RentInvoiceEvent event) {
        kafkaTemplate.send("rent-invoice-events", event.tenantId().toString(), event);
    }
}

package com.reportsystem.delivery.infrastructure.messaging;

import com.reportsystem.shared.dto.event.SaleEvent;
import java.util.UUID;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class DeliveryEventPublisher {

    private final KafkaTemplate<String, Object> kafka;

    public DeliveryEventPublisher(KafkaTemplate<String, Object> kafka) {
        this.kafka = kafka;
    }

    public void publishDeliveryEvent(SaleEvent event) {
        kafka.send("delivery-events", event.tenantId().toString(), event);
    }
}

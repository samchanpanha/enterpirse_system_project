package com.reportsystem.restaurant.infrastructure.messaging;

import com.reportsystem.shared.dto.event.SaleEvent;
import java.util.UUID;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class RestaurantEventPublisher {

    private final KafkaTemplate<String, Object> kafka;

    public RestaurantEventPublisher(KafkaTemplate<String, Object> kafka) {
        this.kafka = kafka;
    }

    public void publishSaleEvent(SaleEvent event) {
        kafka.send("sale-events", event.tenantId().toString(), event);
    }
}

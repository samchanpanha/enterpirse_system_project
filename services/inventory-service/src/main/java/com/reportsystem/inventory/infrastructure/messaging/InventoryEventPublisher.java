package com.reportsystem.inventory.infrastructure.messaging;

import com.reportsystem.shared.dto.event.SaleEvent;
import com.reportsystem.shared.dto.event.StockEvent;
import com.reportsystem.shared.dto.event.StockTransferEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventPublisher {

    private final KafkaTemplate<String, Object> kafka;

    public InventoryEventPublisher(KafkaTemplate<String, Object> kafka) {
        this.kafka = kafka;
    }

    public void publishStockReceived(StockEvent event) {
        kafka.send("stock.received", event.tenantId().toString(), event);
    }

    public void publishLowStockAlert(String tenantId, String productId, java.math.BigDecimal currentQty, java.math.BigDecimal threshold) {
        kafka.send("stock.low", tenantId, java.util.Map.of("tenantId", tenantId, "productId", productId, "currentQty", currentQty, "threshold", threshold));
    }

    public void publishStockTransferEvent(StockTransferEvent event) {
        String topic = switch (event.eventType()) {
            case StockTransferEvent.TYPE_REQUESTED -> "stock.transfer.requested";
            case StockTransferEvent.TYPE_SHIPPED -> "stock.transfer.shipped";
            case StockTransferEvent.TYPE_RECEIVED -> "stock.transfer.received";
            case StockTransferEvent.TYPE_CANCELLED -> "stock.transfer.cancelled";
            default -> "stock.transfer.events";
        };
        kafka.send(topic, event.tenantId().toString(), event);
        // Also send to a generic topic for consumers that want all events
        kafka.send("stock.transfer.events", event.tenantId().toString(), event);
    }

    @KafkaListener(topics = "sale-events")
    public void handleSaleEvent(SaleEvent event) {
        // Deduct stock from warehouse for each item sold
    }
}

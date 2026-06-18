package com.reportsystem.restaurant.infrastructure.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reportsystem.restaurant.domain.model.Order;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class KdsWebSocketHandler extends TextWebSocketHandler {

    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        // KDS clients send status updates (preparing/serving/completed)
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
    }

    public void broadcastNewOrder(Order order) {
        try {
            String payload = mapper.writeValueAsString(Map.of(
                    "type", "new_order",
                    "order", order
            ));
            for (WebSocketSession s : sessions.values()) {
                if (s.isOpen()) s.sendMessage(new TextMessage(payload));
            }
        } catch (Exception e) {
            // log
        }
    }

    public void broadcastOrderUpdate(Order order) {
        try {
            String payload = mapper.writeValueAsString(Map.of(
                    "type", "order_update",
                    "order", order
            ));
            for (WebSocketSession s : sessions.values()) {
                if (s.isOpen()) s.sendMessage(new TextMessage(payload));
            }
        } catch (Exception e) {
            // log
        }
    }
}

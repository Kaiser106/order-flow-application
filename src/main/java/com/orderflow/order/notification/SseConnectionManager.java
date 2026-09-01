package com.orderflow.order.notification;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SseConnectionManager {
    private final Map<UUID, SseEmitter> connections = new ConcurrentHashMap<>();

    public void addConnection(UUID orderId, SseEmitter emitter) {
        connections.put(orderId, emitter);
    }

    public void removeConnection(UUID orderId) {
        connections.remove(orderId);
    }

    public SseEmitter getConnection(UUID orderId) {
        return connections.get(orderId);
    }
}
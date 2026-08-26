package com.orderflow.notification.service;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
@Component
public class SseConnectionManager {

    // Thread-Safe (Eşzamanlı) çalışmaya uygun bir Map kullanıyoruz.
    // Anahtar (Key): Sipariş ID, Değer (Value): SSE Bağlantısı.
    private final Map<Long, SseEmitter> connections = new ConcurrentHashMap<>();

    public void addConnection(Long orderId, SseEmitter emitter) {
        connections.put(orderId, emitter);
    }

    public void removeConnection(Long orderId) {
        // İstemci bağlantıyı kopardığında (veya timeout olduğunda) objeyi Map'ten mutlaka silmeliyiz.
        // Aksi takdirde "Memory Leak" (Hafıza Sızıntısı) oluşur ve sunucu bir süre sonra çöker.
        connections.remove(orderId);
    }

    public SseEmitter getConnection(Long orderId) {
        return connections.get(orderId);
    }
}

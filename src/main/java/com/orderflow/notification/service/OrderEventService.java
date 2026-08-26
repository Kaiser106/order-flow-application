package com.orderflow.notification.service;

import com.orderflow.order.entity.OrderStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderEventService {

    private final SseConnectionManager sseConnectionManager;
    private final MessageSource messageSource; // i18n çevirileri için (messages.properties)

    public void sendOrderUpdate(Long orderId, OrderStatus status) {
        SseEmitter emitter = sseConnectionManager.getConnection(orderId);


        if (emitter != null) {
            try {

                Locale locale = LocaleContextHolder.getLocale();


                String messageKey = "order.status." + status.name().toLowerCase();
                String localizedMessage = messageSource.getMessage(messageKey, null, messageKey, locale);


                SseEmitter.SseEventBuilder event = SseEmitter.event()
                        .id(String.valueOf(orderId))
                        .name(status.name())
                        .data(new SseEventData(orderId, status.name(), localizedMessage, OffsetDateTime.now().toString()));


                emitter.send(event);

            } catch (IOException e) {

                log.error("SSE connection error for order {}. Removing connection.", orderId);
                sseConnectionManager.removeConnection(orderId);
            }
        }
    }


    public record SseEventData(Long orderId, String status, String message, String timestamp) {}
}
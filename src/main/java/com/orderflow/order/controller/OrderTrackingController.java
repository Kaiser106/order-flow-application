package com.orderflow.order.controller;

import com.orderflow.auth.service.CustomUserDetails;
import com.orderflow.common.exception.ForbiddenException;
import com.orderflow.common.exception.ResourceNotFoundException;
import com.orderflow.customer.contract.CustomerContract;
import com.orderflow.order.notification.SseConnectionManager;
import com.orderflow.order.entity.Order;
import com.orderflow.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import java.util.UUID;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderTrackingController {

    private final OrderRepository orderRepository;
    private final SseConnectionManager sseConnectionManager;
    private final CustomerContract customerContract;

    @GetMapping(value = "/{orderId}/tracking", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter trackOrder(
            @PathVariable UUID orderId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("order.not.found"));

        UUID customerId = customerContract.getCustomerIdByUserId(userDetails.getUser().getId());


        if (!order.getCustomer().getId().equals(customerId)) {
            throw new ForbiddenException("system.error.forbidden");
        }

        SseEmitter emitter = new SseEmitter(30 * 60 * 1000L);
        sseConnectionManager.addConnection(orderId, emitter);


        emitter.onCompletion(() -> sseConnectionManager.removeConnection(orderId));
        emitter.onTimeout(() -> sseConnectionManager.removeConnection(orderId));
        emitter.onError((e) -> sseConnectionManager.removeConnection(orderId));

        return emitter;
    }
}
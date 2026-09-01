package com.orderflow.order.dto;
import java.math.BigDecimal;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        UUID customerId,
        UUID restaurantId,
        String status,
        BigDecimal totalPrice,
        String deliveryAddress
) {}
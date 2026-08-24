package com.orderflow.order.dto;

import java.math.BigDecimal;

public record OrderResponse(
        Long id,
        Long customerId,
        Long restaurantId,
        String status,
        BigDecimal totalPrice,
        String deliveryAddress
) {}
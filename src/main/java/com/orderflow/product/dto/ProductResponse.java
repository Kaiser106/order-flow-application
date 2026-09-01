package com.orderflow.product.dto;
import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        UUID restaurantId,
        String name,
        String description,
        BigDecimal price,
        String category,
        boolean available,
        String imageUrl
) {}
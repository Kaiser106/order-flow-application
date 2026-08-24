package com.orderflow.product.dto;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        Long restaurantId,
        String name,
        String description,
        BigDecimal price,
        String category,
        boolean available,
        String imageUrl
) {}
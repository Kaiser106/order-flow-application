package com.orderflow.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;

public record CreateProductRequest(
        @NotNull
        Long restaurantId,

        @NotBlank
        String name,

        String description,

        @NotNull @Positive(message = "{validation.field.positive}")
        BigDecimal price,

        @NotBlank
        String category,

        String imageUrl
) {}
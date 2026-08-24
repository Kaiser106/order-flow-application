package com.orderflow.restaurant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record CreateRestaurantRequest(
        @NotBlank(message = "{validation.field.notblank}")
        String name,

        String description,

        @NotBlank(message = "{validation.field.notblank}")
        String phone,

        @NotBlank @Email(message = "{validation.email.invalid}")
        String email,

        // JSONB alanlarımız için esnek Map yapısı
        @NotNull(message = "{validation.field.notblank}")
        Map<String, Object> address,

        @NotNull(message = "{validation.field.notblank}")
        Map<String, Object> workingHours
) {
}
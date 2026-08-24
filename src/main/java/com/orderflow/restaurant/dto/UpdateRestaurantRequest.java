package com.orderflow.restaurant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public record UpdateRestaurantRequest(
        @NotBlank(message = "{validation.field.notblank}")
        String name,

        String description,

        @NotBlank(message = "{validation.field.notblank}")
        String phone,

        @NotBlank(message = "{validation.field.notblank}")
        @Email(message = "{validation.email.invalid}")
        String email,

        @NotNull(message = "{validation.field.notblank}")
        Map<String, Object> address,

        @NotNull(message = "{validation.field.notblank}")
        Map<String, Object> workingHours,


        @NotNull(message = "{validation.field.notblank}")
        Boolean active
) {
}
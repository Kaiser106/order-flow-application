package com.orderflow.restaurant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateRestaurantRequest(
        @NotBlank(message = "{validation.field.notblank}")
        String name,

        String description,

        @NotBlank(message = "{validation.field.notblank}")
        String phone,

        @NotBlank(message = "{validation.field.notblank}")
        @Email(message = "{validation.email.invalid}")
        String email,

        @NotBlank(message = "{validation.field.notblank}")
        String address,

        @NotBlank(message = "{validation.field.notblank}")
        String workingHours,

        @NotNull(message = "{validation.field.notblank}")
        Boolean active
) {}
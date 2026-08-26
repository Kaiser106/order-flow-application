package com.orderflow.restaurant.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateRestaurantRequest(
        @NotBlank(message = "{validation.field.notblank}")
        String name,
        String description,
        @NotBlank(message = "{validation.field.notblank}")
        String phone,
        @NotBlank @Email(message = "{validation.email.invalid}")
        String email,


        @NotBlank(message = "{validation.field.notblank}")
        String address,

        @NotBlank(message = "{validation.field.notblank}")
        String workingHours
) {}
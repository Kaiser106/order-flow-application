package com.orderflow.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "{validation.email.invalid}")
        @Email(message = "{validation.email.invalid}")
        String email,
        @NotBlank(message = "{validation.password.required}")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,
        @NotBlank(message = "{validation.field.notblank}")
        String firstName,
        @NotBlank(message = "{validation.field.notblank}")
        String lastName,
        @NotBlank(message = "{validation.field.notblank}")
        String phone,
        @NotBlank(message = "{validation.field.notblank}")
        String address
) {}
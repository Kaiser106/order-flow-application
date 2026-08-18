package com.orderflow.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
public record RegisterRequest(
        @NotBlank(message = "{validation.email.invalid}") // Mesaj i18n dosyasından okunur
        @Email(message = "{validation.email.invalid}")
        String email,

        @NotBlank(message = "{validation.password.required}")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password
) {}

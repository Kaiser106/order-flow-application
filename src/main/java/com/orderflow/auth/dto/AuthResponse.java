package com.orderflow.auth.dto;

public record AuthResponse(
        Long id,
        String email,
        String role
) {}
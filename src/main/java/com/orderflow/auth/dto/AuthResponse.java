package com.orderflow.auth.dto;
import java.util.UUID;
public record AuthResponse(UUID id, String email, String role) {}
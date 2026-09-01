package com.orderflow.restaurant.dto;
import java.util.Map;
import java.util.UUID;

public record RestaurantResponse(
        UUID id,
        String name,
        String description,
        String phone,
        String email,
        Map<String, Object> address,
        Map<String, Object> workingHours,
        boolean active
) {}
package com.orderflow.restaurant.dto;

import java.util.Map;


public record RestaurantResponse(
        Long id,
        String name,
        String description,
        String phone,
        String email,
        Map<String, Object> address,
        Map<String, Object> workingHours,
        boolean active
) {}
package com.orderflow.restaurant.contracts.impl;

import com.orderflow.auth.entity.User;
import com.orderflow.restaurant.contracts.RestaurantContract;
import com.orderflow.restaurant.entity.Restaurant;
import com.orderflow.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantContractImpl implements RestaurantContract {
    private final RestaurantRepository restaurantRepository;

    @Override
    public boolean isRestaurantActive(UUID restaurantId) {
        return restaurantRepository.findById(restaurantId)
                .map(Restaurant::isActive)
                .orElse(false);
    }

    @Override
    public UUID getRestaurantIdByUserId(UUID userId) {
        return restaurantRepository.findByUserId(userId)
                .map(Restaurant::getId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant profile not found for user: " + userId));
    }

    @Override
    public void createDefaultRestaurant(User user, String firstName, String email, String phone) {
        Restaurant restaurant = new Restaurant();
        restaurant.setUser(user);
        restaurant.setName(firstName + " İşletmesi");
        restaurant.setEmail(email);
        restaurant.setPhone(phone);
        restaurant.setDescription("Yeni İşletme");
        restaurant.setAddress(Map.of("city", "Belirtilmedi", "district", "Belirtilmedi"));
        restaurant.setWorkingHours(Map.of("open", "09:00", "close", "22:00"));
        restaurant.setActive(true);
        restaurantRepository.save(restaurant);
    }
}
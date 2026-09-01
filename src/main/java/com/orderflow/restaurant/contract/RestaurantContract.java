package com.orderflow.restaurant.contract;
import com.orderflow.auth.entity.User;
import java.util.UUID;

public interface RestaurantContract {
    boolean isRestaurantActive(UUID restaurantId);
    UUID getRestaurantIdByUserId(UUID userId);
    void createDefaultRestaurant(User user, String firstName, String email, String phone);
}
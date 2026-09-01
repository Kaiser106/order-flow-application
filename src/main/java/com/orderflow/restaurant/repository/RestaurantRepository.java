package com.orderflow.restaurant.repository;
import com.orderflow.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface RestaurantRepository extends JpaRepository<Restaurant, UUID>{
    boolean existsByIdAndActiveTrue(UUID id);
    Optional<Restaurant> findByUserId(UUID userId);
}
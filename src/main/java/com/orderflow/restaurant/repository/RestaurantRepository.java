package com.orderflow.restaurant.repository;

import com.orderflow.restaurant.entity.Restaurant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long>{
    boolean existsByIdAndActiveTrue(Long id);
}

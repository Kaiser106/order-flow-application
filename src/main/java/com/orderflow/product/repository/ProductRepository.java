package com.orderflow.product.repository;

import com.orderflow.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByRestaurantIdAndAvaliableTrue(Long restaurantId);

    Optional<Product> findByIdAndRestaurantIdAndAvaliableTrue(Long id, Long restaurantId);
}

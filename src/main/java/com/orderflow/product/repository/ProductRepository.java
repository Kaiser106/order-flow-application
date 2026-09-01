package com.orderflow.product.repository;
import com.orderflow.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByRestaurantIdAndAvailableTrue(UUID restaurantId);
    Optional<Product> findByIdAndRestaurantIdAndAvailableTrue(UUID id, UUID restaurantId);
}
package com.orderflow.product.contracts.impl;

import com.orderflow.product.contracts.ProductContract;
import com.orderflow.product.entity.Product;
import com.orderflow.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductContractImpl implements ProductContract {
    private final ProductRepository productRepository;

    @Override
    public BigDecimal getActiveProductPrice(UUID productId, UUID restaurantId) {
        return productRepository.findByIdAndRestaurantIdAndAvailableTrue(productId, restaurantId)
                .map(Product::getPrice)
                .orElseThrow(() -> new IllegalArgumentException("Product is not available"));
    }

    @Override
    public boolean isProductAvailableAndBelongsToRestaurant(UUID productId, UUID restaurantId) {
        return productRepository.findByIdAndRestaurantIdAndAvailableTrue(productId, restaurantId).isPresent();
    }
}
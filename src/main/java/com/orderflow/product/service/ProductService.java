package com.orderflow.product.service;

import com.orderflow.common.result.Result;
import com.orderflow.product.contract.ProductContract;
import com.orderflow.product.dto.CreateProductRequest;
import com.orderflow.product.dto.ProductResponse;
import com.orderflow.product.entity.Product;
import com.orderflow.product.repository.ProductRepository;
import com.orderflow.restaurant.contract.RestaurantContract;
import com.orderflow.restaurant.entity.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List; // Import unutulmasın

@Service
@RequiredArgsConstructor
public class ProductService implements ProductContract {

    private final ProductRepository productRepository;
    private final RestaurantContract restaurantContract;

    @Override
    public BigDecimal getActiveProductPrice(Long productId, Long restaurantId) {
        return productRepository.findByIdAndRestaurantIdAndAvailableTrue(productId, restaurantId)
                .map(Product::getPrice)
                .orElseThrow(() -> new IllegalArgumentException("Product is not available"));
    }

    @Override
    public boolean isProductAvailableAndBelongsToRestaurant(Long productId, Long restaurantId) {
        return productRepository.findByIdAndRestaurantIdAndAvailableTrue(productId, restaurantId).isPresent();
    }

    // YENİ EKLENEN METOT
    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByRestaurantId(Long restaurantId) {
        return productRepository.findByRestaurantIdAndAvailableTrue(restaurantId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    public Result<ProductResponse> createProduct(CreateProductRequest request) {
        if (!restaurantContract.isRestaurantActive(request.restaurantId())) {
            return Result.failure("Restaurant is not active or does not exist.");
        }

        Restaurant restaurantRef = new Restaurant();
        restaurantRef.setId(request.restaurantId());

        Product product = new Product();
        product.setRestaurant(restaurantRef);
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setCategory(request.category());
        product.setAvailable(true);
        product.setImageUrl(request.imageUrl());

        Product savedProduct = productRepository.save(product);
        return Result.success(mapToResponse(savedProduct));
    }

    private ProductResponse mapToResponse(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getRestaurant().getId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getCategory(),
                product.isAvailable(),
                product.getImageUrl()
        );
    }
}
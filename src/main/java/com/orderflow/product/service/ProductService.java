package com.orderflow.product.service;
import com.orderflow.auth.service.CustomUserDetails;
import com.orderflow.common.exception.UnauthorizedException;
import com.orderflow.common.result.Result;
import com.orderflow.product.contract.ProductContract;
import com.orderflow.product.dto.CreateProductRequest;
import com.orderflow.product.dto.ProductResponse;
import com.orderflow.product.entity.Product;
import com.orderflow.product.repository.ProductRepository;
import com.orderflow.restaurant.contract.RestaurantContract;
import com.orderflow.restaurant.entity.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductService implements ProductContract {
    private final ProductRepository productRepository;
    private final RestaurantContract restaurantContract;

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

    @Transactional(readOnly = true)
    public List<ProductResponse> getProductsByRestaurantId(UUID restaurantId) {
        return productRepository.findByRestaurantIdAndAvailableTrue(restaurantId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional
    @PreAuthorize("hasRole('RESTAURANT')")
    public Result<ProductResponse> createProduct(CreateProductRequest request) {
        UUID currentUserId = getCurrentUserIdSafely();
        UUID restaurantId = restaurantContract.getRestaurantIdByUserId(currentUserId);
        if (!restaurantContract.isRestaurantActive(restaurantId)) {
            return Result.failure("Restaurant is not active or does not exist.");
        }
        Restaurant restaurantRef = new Restaurant();
        restaurantRef.setId(restaurantId);
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

    private UUID getCurrentUserIdSafely() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new UnauthorizedException("system.error.unauthorized");
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getId();
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
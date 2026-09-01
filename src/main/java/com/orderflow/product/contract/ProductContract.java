package com.orderflow.product.contract;
import java.math.BigDecimal;
import java.util.UUID;

public interface ProductContract {
    BigDecimal getActiveProductPrice(UUID productId, UUID restaurantId);
    boolean isProductAvailableAndBelongsToRestaurant(UUID productId, UUID restaurantId);
}
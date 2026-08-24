package com.orderflow.product.contract;

import java.math.BigDecimal;

public interface ProductContract {
    BigDecimal getActiveProductPrice(Long productId, Long restaurantId);
    boolean isProductAvailableAndBelongsToRestaurant(Long productId, Long restaurantId);

}

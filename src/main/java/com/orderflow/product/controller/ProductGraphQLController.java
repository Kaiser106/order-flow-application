package com.orderflow.product.controller;

import com.orderflow.common.exception.BusinessException;
import com.orderflow.common.result.Result;
import com.orderflow.product.dto.CreateProductRequest;
import com.orderflow.product.dto.ProductResponse;
import com.orderflow.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ProductGraphQLController {

    private final ProductService productService;

    @MutationMapping
    public ProductResponse createProduct(@Argument CreateProductRequest input) {
        Result<ProductResponse> result = productService.createProduct(input);

        if (!result.isSuccess()) {
            throw new BusinessException(result.getMessage(), result.getErrorCode());
        }

        return result.getData();
    }
    @QueryMapping
    public List<ProductResponse> products(@Argument Long restaurantId) {
        return productService.getProductsByRestaurantId(restaurantId);
    }
}
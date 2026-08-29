package com.orderflow.restaurant.controller;

import com.orderflow.common.dto.PageResponse;
import com.orderflow.common.exception.BusinessException;
import com.orderflow.common.result.Result;
import com.orderflow.restaurant.dto.CreateRestaurantRequest;
import com.orderflow.restaurant.dto.RestaurantResponse;
import com.orderflow.restaurant.dto.UpdateRestaurantRequest;
import com.orderflow.restaurant.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class RestaurantGraphQLController {

    private final RestaurantService restaurantService;


    @MutationMapping
    public RestaurantResponse updateRestaurant(@Argument UpdateRestaurantRequest input) {
        Result<RestaurantResponse> result = restaurantService.updateRestaurant(input);
        if (!result.isSuccess()) {
            throw new BusinessException(result.getMessage(), result.getErrorCode());
        }
        return result.getData();
    }

    @QueryMapping
    public PageResponse<RestaurantResponse> restaurants(
            @Argument Integer page,
            @Argument Integer size) {

        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? size : 10;

        return restaurantService.getRestaurants(pageNumber, pageSize).getData();
    }
}
package com.orderflow.restaurant.service;

import com.orderflow.common.result.Result;
import com.orderflow.restaurant.contract.RestaurantContract;
import com.orderflow.restaurant.dto.CreateRestaurantRequest;
import com.orderflow.restaurant.dto.RestaurantResponse;
import com.orderflow.restaurant.entity.Restaurant;
import com.orderflow.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class RestaurantService implements RestaurantContract {
    private final RestaurantRepository restaurantRepository;

    @Override
    public boolean isRestaurantActive(Long restaurantId) {


        return restaurantRepository.existsByIdAndActiveTrue(restaurantId);
    }
    @Transactional
    public Result<RestaurantResponse> createRestaurant(CreateRestaurantRequest request){
        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.name());
        restaurant.setDescription(request.description());
        restaurant.setPhone(request.phone());
        restaurant.setEmail(request.email());
        restaurant.setAddress(request.address());
        restaurant.setWorkingHours(request.workingHours());
        restaurant.setActive(true);
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return Result.success(mapToResponse(savedRestaurant),"Restaurant created successfully.");

    }

    @Transactional(readOnly = true)
    public Result<RestaurantResponse> getRestaurantById(Long id) {
        return restaurantRepository.findById(id)
                .map(restaurant -> Result.success(mapToResponse(restaurant)))
                .orElseThrow(() -> new IllegalArgumentException("Restaurant not found"));
    }

    private RestaurantResponse mapToResponse(Restaurant restaurant) {
        return new RestaurantResponse(
                restaurant.getId(),
                restaurant.getName(),
                restaurant.getDescription(),
                restaurant.getPhone(),
                restaurant.getEmail(),
                restaurant.getAddress(),
                restaurant.getWorkingHours(),
                restaurant.isActive()
        );
    }
}

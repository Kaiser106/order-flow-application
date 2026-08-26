package com.orderflow.restaurant.service;

import com.orderflow.common.constant.PaginationConstants;
import com.orderflow.common.dto.PageResponse;
import com.orderflow.common.result.Result;
import com.orderflow.restaurant.contract.RestaurantContract;
import com.orderflow.restaurant.dto.CreateRestaurantRequest;
import com.orderflow.restaurant.dto.RestaurantResponse;
import com.orderflow.restaurant.entity.Restaurant;
import com.orderflow.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
@Service
@RequiredArgsConstructor
public class RestaurantService implements RestaurantContract {

    private final RestaurantRepository restaurantRepository;

    @Override
    public boolean isRestaurantActive(Long restaurantId) {
        return restaurantRepository.existsByIdAndActiveTrue(restaurantId);
    }

    @Transactional
    public Result<RestaurantResponse> createRestaurant(CreateRestaurantRequest request) {
        Restaurant restaurant = new Restaurant();
        restaurant.setName(request.name());
        restaurant.setDescription(request.description());
        restaurant.setPhone(request.phone());
        restaurant.setEmail(request.email());

        // Gelen JSON String'leri Jackson kütüphanesi ile Map objelerine dönüştürüyoruz
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> addressMap = mapper.readValue(request.address(), new TypeReference<Map<String, Object>>() {});
            Map<String, Object> workingHoursMap = mapper.readValue(request.workingHours(), new TypeReference<Map<String, Object>>() {});

            restaurant.setAddress(addressMap);
            restaurant.setWorkingHours(workingHoursMap);
        } catch (Exception e) {
            // Eğer istemci hatalı bir JSON formatı gönderirse şık bir hata döneriz
            return Result.failure("Invalid JSON format for address or working hours.", "ERR_JSON_PARSE");
        }

        restaurant.setActive(true);
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return Result.success(mapToResponse(savedRestaurant), "Restaurant created successfully.");
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

    @Transactional(readOnly = true)
    public Result<PageResponse<RestaurantResponse>> getRestaurants(int page, int size) {
        // İstemcinin çok büyük bir size gönderip veritabanını çökertmesini engelliyoruz.
        int validSize = Math.min(size, PaginationConstants.MAX_PAGE_SIZE);
        int validPage = Math.max(page, PaginationConstants.DEFAULT_PAGE_NUMBER);

        Pageable pageable = PageRequest.of(validPage, validSize);

        // JpaRepository bizim için sayfalamayı (LIMIT ve OFFSET SQL komutlarını) otomatik yapar.
        Page<Restaurant> restaurantPage = restaurantRepository.findAll(pageable);

        // Entity'leri DTO'ya dönüştürüyoruz (map)
        Page<RestaurantResponse> responsePage = restaurantPage.map(this::mapToResponse);

        return Result.success(PageResponse.of(responsePage));
    }
}
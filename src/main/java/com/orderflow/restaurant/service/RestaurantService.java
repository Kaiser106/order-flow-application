package com.orderflow.restaurant.service;

import com.orderflow.auth.enums.Role;
import com.orderflow.auth.service.CustomUserDetails;
import com.orderflow.common.constant.PaginationConstants;
import com.orderflow.common.dto.PageResponse;
import com.orderflow.common.exception.ForbiddenException;
import com.orderflow.common.exception.UnauthorizedException;
import com.orderflow.common.result.Result;
import com.orderflow.restaurant.contract.RestaurantContract;
import com.orderflow.restaurant.dto.RestaurantResponse;
import com.orderflow.restaurant.dto.UpdateRestaurantRequest;
import com.orderflow.restaurant.entity.Restaurant;
import com.orderflow.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
    public Result<RestaurantResponse> updateRestaurant(UpdateRestaurantRequest request) {
        Long currentUserId = getCurrentUserIdSafely();

        Restaurant restaurant = restaurantRepository.findByUserId(currentUserId)
                .orElseThrow(() -> new IllegalArgumentException("İşletme profili bulunamadı."));

        restaurant.setName(request.name());
        restaurant.setDescription(request.description());
        restaurant.setPhone(request.phone());
        restaurant.setEmail(request.email());

        try {
            ObjectMapper mapper = new ObjectMapper();


            Map<String, Object> addressMap = mapper.readValue(request.address(), new TypeReference<Map<String, Object>>() {});
            Map<String, Object> workingHoursMap = mapper.readValue(request.workingHours(), new TypeReference<Map<String, Object>>() {});

            restaurant.setAddress(addressMap);
            restaurant.setWorkingHours(workingHoursMap);
        } catch (Exception e) {
            return Result.failure("Adres veya çalışma saatleri formatı hatalı.", "ERR_JSON_PARSE");
        }

        restaurant.setActive(request.active());
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return Result.success(mapToResponse(savedRestaurant), "Restaurant updated successfully.");
    }


    private Long getCurrentUserIdSafely() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new UnauthorizedException("system.error.unauthorized");
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

        if(userDetails.getUser().getRole() != Role.RESTAURANT) {
            throw new ForbiddenException("Sadece işletme hesapları restoran bilgilerini güncelleyebilir.");
        }
        return userDetails.getUser().getId();
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

        int validSize = Math.min(size, PaginationConstants.MAX_PAGE_SIZE);
        int validPage = Math.max(page, PaginationConstants.DEFAULT_PAGE_NUMBER);

        Pageable pageable = PageRequest.of(validPage, validSize);


        Page<Restaurant> restaurantPage = restaurantRepository.findAll(pageable);


        Page<RestaurantResponse> responsePage = restaurantPage.map(this::mapToResponse);

        return Result.success(PageResponse.of(responsePage));
    }
    @Override
    public Long getRestaurantIdByUserId(Long userId) {
        return restaurantRepository.findByUserId(userId)
                .map(Restaurant::getId)
                .orElseThrow(() -> new IllegalArgumentException("Restaurant profile not found for user: " + userId));
    }
}
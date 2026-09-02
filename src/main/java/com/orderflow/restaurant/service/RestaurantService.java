package com.orderflow.restaurant.service;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import com.orderflow.auth.entity.User;
import com.orderflow.auth.repository.UserRepository;
import com.orderflow.auth.service.CustomUserDetails;
import com.orderflow.common.constant.PaginationConstants;
import com.orderflow.common.dto.PageResponse;
import com.orderflow.common.exception.UnauthorizedException;
import com.orderflow.common.result.Result;
import com.orderflow.restaurant.dto.RestaurantResponse;
import com.orderflow.restaurant.dto.UpdateRestaurantRequest;
import com.orderflow.restaurant.entity.Restaurant;
import com.orderflow.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RestaurantService {
    private final RestaurantRepository restaurantRepository;
    private final UserRepository userRepository;

    @Transactional
    @PreAuthorize("hasRole('RESTAURANT')")
    public Result<RestaurantResponse> updateRestaurant(UpdateRestaurantRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Oturum hatası, kullanıcı bulunamadı."));
        UUID currentUserId = currentUser.getId();

        Restaurant restaurant = restaurantRepository.findByUserId(currentUserId)
                .orElseGet(Restaurant::new);

        restaurant.setUser(userRepository.getReferenceById(currentUserId));
        restaurant.setName(request.name());
        restaurant.setDescription(request.description());
        restaurant.setPhone(request.phone());
        restaurant.setEmail(request.email());

        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> addressMap = mapper.readValue(request.address(), new TypeReference<>() {});
            Map<String, Object> workingHoursMap = mapper.readValue(request.workingHours(), new TypeReference<>() {});
            restaurant.setAddress(addressMap);
            restaurant.setWorkingHours(workingHoursMap);
        } catch (Exception e) {
            return Result.failure("Adres veya çalışma saatleri formatı hatalı.", "ERR_JSON_PARSE");
        }

        restaurant.setActive(request.active());
        Restaurant savedRestaurant = restaurantRepository.save(restaurant);
        return Result.success(mapToResponse(savedRestaurant), "İşletme profili başarıyla güncellendi.");
    }

    private UUID getCurrentUserIdSafely() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || authentication.getPrincipal().equals("anonymousUser")) {
            throw new UnauthorizedException("system.error.unauthorized");
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getId();
    }

    @Transactional(readOnly = true)
    public Result<RestaurantResponse> getRestaurantById(UUID id) {
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
}
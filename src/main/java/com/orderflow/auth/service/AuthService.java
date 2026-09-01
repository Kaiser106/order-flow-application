package com.orderflow.auth.service;

import com.orderflow.auth.dto.AuthResponse;
import com.orderflow.auth.dto.RegisterRequest;
import com.orderflow.auth.entity.User;
import com.orderflow.auth.enums.Role;
import com.orderflow.auth.repository.UserRepository;
import com.orderflow.common.result.Result;
import com.orderflow.customer.contract.CustomerContract;
import com.orderflow.courier.contract.CourierContract;
import com.orderflow.restaurant.contract.RestaurantContract;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerContract customerContract;
    private final CourierContract courierContract;
    private final RestaurantContract restaurantContract;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Result<AuthResponse> register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            return Result.failure("Email is already in use.");
        }

        Role userRole = Role.CUSTOMER;
        if (request.role() != null) {
            if (request.role().equalsIgnoreCase("COURIER")) {
                userRole = Role.COURIER;
            } else if (request.role().equalsIgnoreCase("RESTAURANT")) {
                userRole = Role.RESTAURANT;
            }
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(userRole);
        User savedUser = userRepository.save(user);


        if (userRole == Role.CUSTOMER) {
            customerContract.createCustomer(savedUser, request.firstName(), request.lastName(), request.phone(), request.address());
        } else if (userRole == Role.COURIER) {
            courierContract.createCourier(savedUser, request.firstName(), request.lastName(), request.phone());
        } else if (userRole == Role.RESTAURANT) {
            restaurantContract.createDefaultRestaurant(savedUser, request.firstName(), request.email(), request.phone());
        }

        return Result.success(
                new AuthResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name()),
                "Registration successful."
        );
    }
}
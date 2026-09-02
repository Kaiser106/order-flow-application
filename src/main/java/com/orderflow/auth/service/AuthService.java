package com.orderflow.auth.service;

import com.orderflow.auth.dto.AuthResponse;
import com.orderflow.auth.dto.LoginRequest;
import com.orderflow.auth.dto.RegisterRequest;
import com.orderflow.auth.entity.User;
import com.orderflow.auth.enums.Role;
import com.orderflow.auth.repository.UserRepository;
import com.orderflow.common.result.Result;
import com.orderflow.customer.contracts.CustomerContract;
import com.orderflow.courier.contracts.CourierContract;
import com.orderflow.restaurant.contracts.RestaurantContract;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;


    private final CustomerContract customerContract;
    private final CourierContract courierContract;
    private final RestaurantContract restaurantContract;

    @Transactional
    public Result<AuthResponse> register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            return Result.failure("Email already in use");
        }

        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));

        // Rol belirtilmemişse varsayılan olarak CUSTOMER yapıyoruz
        Role userRole = request.role() != null ? Role.valueOf(request.role().toUpperCase()) : Role.CUSTOMER;
        user.setRole(userRole);

        User savedUser = userRepository.save(user);

        // KULLANICI ROLÜNE GÖRE İLGİLİ PROFİLİ OTOMATİK OLUŞTUR (Sihir Burada!)
        switch (userRole) {
            case CUSTOMER -> customerContract.createCustomer(savedUser, request.firstName(), request.lastName(), request.phone(), request.address());
            case COURIER -> courierContract.createCourier(savedUser, request.firstName(), request.lastName(), request.phone());
            case RESTAURANT -> restaurantContract.createDefaultRestaurant(savedUser, request.firstName(), request.email(), request.phone());
        }

        return Result.success(new AuthResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name()), "User registered successfully");
    }

    public Result<AuthResponse> login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        return Result.success(new AuthResponse(user.getId(), user.getEmail(), user.getRole().name()), "Login successful");
    }
}
package com.orderflow.auth.service;

import com.orderflow.auth.dto.AuthResponse;
import com.orderflow.auth.dto.RegisterRequest;
import com.orderflow.auth.entity.User;
import com.orderflow.auth.enums.Role;
import com.orderflow.auth.repository.UserRepository;
import com.orderflow.common.result.Result;
import com.orderflow.customer.entity.Customer;
import com.orderflow.customer.repository.CustomerRepository;
import com.orderflow.courier.entity.Courier;
import com.orderflow.courier.repository.CourierRepository;
import com.orderflow.restaurant.entity.Restaurant;
import com.orderflow.restaurant.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CourierRepository courierRepository;
    private final RestaurantRepository restaurantRepository;
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
            Customer customer = new Customer();
            customer.setUser(savedUser);
            customer.setFirstname(request.firstName());
            customer.setLastName(request.lastName());
            customer.setPhone(request.phone());
            customer.setAddress(request.address() != null ? request.address() : "Belirtilmemiş");
            customerRepository.save(customer);

        } else if (userRole == Role.COURIER) {
            Courier courier = new Courier();
            courier.setUser(savedUser);
            courier.setFirstName(request.firstName());
            courier.setLastName(request.lastName());
            courier.setPhone(request.phone());
            courier.setActive(true);
            courierRepository.save(courier);

        } else if (userRole == Role.RESTAURANT) {
            Restaurant restaurant = new Restaurant();
            restaurant.setUser(savedUser);
            restaurant.setName(request.firstName() + " İşletmesi");
            restaurant.setEmail(request.email());
            restaurant.setPhone(request.phone());
            restaurant.setDescription("Yeni İşletme");


            restaurant.setAddress(Map.of("city", "Belirtilmedi", "district", "Belirtilmedi"));
            restaurant.setWorkingHours(Map.of("open", "09:00", "close", "22:00"));
            restaurant.setActive(true);

            restaurantRepository.save(restaurant);
        }


        return Result.success(
                new AuthResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name()),
                "Registration successful."
        );
    }
}
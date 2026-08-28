package com.orderflow.auth.service;

import com.orderflow.auth.dto.AuthResponse;
import com.orderflow.auth.dto.RegisterRequest;
import com.orderflow.auth.entity.User;
import com.orderflow.auth.enums.Role;
import com.orderflow.auth.repository.UserRepository;
import com.orderflow.common.result.Result;
import com.orderflow.customer.entity.Customer;
import com.orderflow.customer.repository.CustomerRepository;
import com.orderflow.courier.entity.Courier; // Eklendi
import com.orderflow.courier.repository.CourierRepository; // Eklendi
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CourierRepository courierRepository; // Eklendi
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Result<AuthResponse> register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            return Result.failure("Email is already in use.");
        }


        Role userRole = (request.role() != null && request.role().equalsIgnoreCase("COURIER"))
                ? Role.COURIER
                : Role.CUSTOMER;


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
            courier.setActive(true); // Test için kuryeyi direkt aktif yapıyoruz
            courierRepository.save(courier);
        }

        return Result.success(
                new AuthResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name()),
                "Registration successful."
        );
    }
}
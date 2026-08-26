package com.orderflow.auth.service;

import com.orderflow.auth.dto.AuthResponse;
import com.orderflow.auth.dto.RegisterRequest;
import com.orderflow.auth.entity.User;
import com.orderflow.auth.enums.Role;
import com.orderflow.auth.repository.UserRepository;
import com.orderflow.common.result.Result;
import com.orderflow.customer.entity.Customer;
import com.orderflow.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public Result<AuthResponse> register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            return Result.failure("Email is already in use.");
        }


        User user = new User();
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(Role.CUSTOMER);
        User savedUser = userRepository.save(user);


        Customer customer = new Customer();
        customer.setUser(savedUser);
        customer.setFirstname(request.firstName());
        customer.setLastName(request.lastName());
        customer.setPhone(request.phone());
        customer.setAddress(request.address());
        customerRepository.save(customer);

        return Result.success(
                new AuthResponse(savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name()),
                "Registration successful."
        );
    }
}
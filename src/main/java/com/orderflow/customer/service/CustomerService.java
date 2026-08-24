package com.orderflow.customer.service;

import com.orderflow.customer.contract.CustomerContract;
import com.orderflow.customer.entity.Customer;
import com.orderflow.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor

public class CustomerService implements CustomerContract {
    private final CustomerRepository customerRepository;

    @Override
    public Long getCustomerIdByUserId(Long userId) {
        return customerRepository.findByUserId(userId)
                .map(Customer::getId)
                .orElseThrow(() -> new IllegalArgumentException("Customer profile not found for user: " + userId));
    }
}

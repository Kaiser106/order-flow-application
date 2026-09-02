package com.orderflow.customer.contracts.impl;

import com.orderflow.auth.entity.User;
import com.orderflow.customer.contracts.CustomerContract;
import com.orderflow.customer.entity.Customer;
import com.orderflow.customer.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomerContractImpl implements CustomerContract {
    private final CustomerRepository customerRepository;

    @Override
    public UUID getCustomerIdByUserId(UUID userId) {
        return customerRepository.findByUserId(userId)
                .map(Customer::getId)
                .orElseThrow(() -> new IllegalArgumentException("Customer profile not found for user: " + userId));
    }

    @Override
    public void createCustomer(User user, String firstName, String lastName, String phone, String address) {
        Customer customer = new Customer();
        customer.setUser(user);
        customer.setFirstname(firstName);
        customer.setLastName(lastName);
        customer.setPhone(phone);
        customer.setAddress(address != null ? address : "Belirtilmemiş");
        customerRepository.save(customer);
    }
}
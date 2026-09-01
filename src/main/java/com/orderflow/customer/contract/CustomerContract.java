package com.orderflow.customer.contract;
import com.orderflow.auth.entity.User;
import java.util.UUID;

public interface CustomerContract {
    UUID getCustomerIdByUserId(UUID userId);
    void createCustomer(User user, String firstName, String lastName, String phone, String address);
}
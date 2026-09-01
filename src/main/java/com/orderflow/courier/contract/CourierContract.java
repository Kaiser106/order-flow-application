package com.orderflow.courier.contract;
import com.orderflow.auth.entity.User;
import java.util.UUID;

public interface CourierContract {
    UUID getCourierIdByUserId(UUID userId);
    boolean isCourierActive(UUID courierId);
    void createCourier(User user, String firstName, String lastName, String phone);
}
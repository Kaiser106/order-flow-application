package com.orderflow.courier.service;
import com.orderflow.auth.entity.User;
import com.orderflow.courier.contract.CourierContract;
import com.orderflow.courier.entity.Courier;
import com.orderflow.courier.repository.CourierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourierService implements CourierContract {
    private final CourierRepository courierRepository;

    @Override
    public UUID getCourierIdByUserId(UUID userId) {
        return courierRepository.findByUserId(userId)
                .map(Courier::getId)
                .orElseThrow(() -> new IllegalArgumentException("Courier profile not found for user: " + userId));
    }

    @Override
    public boolean isCourierActive(UUID courierId) {
        return courierRepository.findById(courierId)
                .map(Courier::getActive)
                .orElse(false);
    }

    @Override
    public void createCourier(User user, String firstName, String lastName, String phone) {
        Courier courier = new Courier();
        courier.setUser(user);
        courier.setFirstName(firstName);
        courier.setLastName(lastName);
        courier.setPhone(phone);
        courier.setActive(true);
        courierRepository.save(courier);
    }
}
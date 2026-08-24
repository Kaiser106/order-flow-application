package com.orderflow.courier.service;

import com.orderflow.courier.contract.CourierContract;
import com.orderflow.courier.entity.Courier;
import com.orderflow.courier.repository.CourierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CourierService implements CourierContract {
    private final CourierRepository courierRepository;


    @Override
    public Long getCourierIdByUserId(Long userId) {
        return courierRepository.findByUserId(userId)
                .map(Courier::getId)
                .orElseThrow(() -> new IllegalArgumentException("Courier profile not found for user: " + userId));
    }

    @Override
    public boolean isCourierActive(Long courierId) {
        return courierRepository.findById(courierId)
                .map(Courier::getActive)
                .orElse(false);
    }
}


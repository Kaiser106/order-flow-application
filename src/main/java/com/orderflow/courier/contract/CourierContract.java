package com.orderflow.courier.contract;

public interface CourierContract {
    Long getCourierIdByUserId(Long userId);
    boolean isCourierActive(Long courierId);
}

package com.orderflow.order.repository;

import com.orderflow.order.entity.Order;
import com.orderflow.order.entity.OrderStatus; // Bu importu ekle
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Page<Order> findByCustomerId(Long customerId, Pageable pageable);
    Page<Order> findByRestaurantId(Long restaurantId, Pageable pageable);


    Page<Order> findByStatusAndCourierIsNull(OrderStatus status, Pageable pageable);


    Page<Order> findByCourierId(Long courierId, Pageable pageable);
}
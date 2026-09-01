package com.orderflow.order.repository;
import com.orderflow.order.entity.Order;
import com.orderflow.order.entity.OrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
    Page<Order> findByCustomerId(UUID customerId, Pageable pageable);
    Page<Order> findByRestaurantId(UUID restaurantId, Pageable pageable);
    Page<Order> findByStatusAndCourierIsNull(OrderStatus status, Pageable pageable);
    Page<Order> findByCourierId(UUID courierId, Pageable pageable);
}
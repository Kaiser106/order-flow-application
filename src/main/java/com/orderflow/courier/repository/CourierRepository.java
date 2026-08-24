package com.orderflow.courier.repository;


import com.orderflow.courier.entity.Courier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CourierRepository extends JpaRepository<Courier, Long> {
    Optional<Courier> findByUserId(Long userId);
}

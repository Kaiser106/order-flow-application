package com.orderflow.order.service;

import com.orderflow.auth.service.CustomUserDetails;
import com.orderflow.common.result.Result;
import com.orderflow.customer.contract.CustomerContract;
import com.orderflow.customer.entity.Customer;
import com.orderflow.order.dto.CreateOrderRequest;
import com.orderflow.order.dto.OrderItemRequest;
import com.orderflow.order.dto.OrderResponse;
import com.orderflow.order.entity.Order;
import com.orderflow.order.entity.OrderItem;
import com.orderflow.order.entity.OrderStatus;
import com.orderflow.order.repository.OrderRepository;
import com.orderflow.product.contract.ProductContract;
import com.orderflow.product.entity.Product;
import com.orderflow.restaurant.contract.RestaurantContract;
import com.orderflow.restaurant.entity.Restaurant;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;


    private final CustomerContract customerContract;
    private final RestaurantContract restaurantContract;
    private final ProductContract productContract;


    @Transactional
    public Result<OrderResponse> createOrder(CreateOrderRequest request) {

        // 1. O an oturum açmış (authenticated) kullanıcıyı Session/SecurityContext'ten alıyoruz.
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        Long currentUserId = userDetails.getUser().getId();


        Long customerId = customerContract.getCustomerIdByUserId(currentUserId);


        if (!restaurantContract.isRestaurantActive(request.restaurantId())) {
            return Result.failure("restaurant.not.active", "ERR_REST_01"); // İleride i18n mesajları ile eşleşecek
        }


        Customer customerRef = new Customer();
        customerRef.setId(customerId);

        Restaurant restaurantRef = new Restaurant();
        restaurantRef.setId(request.restaurantId());


        Order order = new Order();
        order.setCustomer(customerRef);
        order.setRestaurant(restaurantRef);
        order.setStatus(OrderStatus.PENDING);
        order.setDeliveryAddress(request.deliveryAddress());

        BigDecimal totalOrderPrice = BigDecimal.ZERO;


        for (OrderItemRequest itemRequest : request.items()) {


            if (!productContract.isProductAvailableAndBelongsToRestaurant(itemRequest.productId(), request.restaurantId())) {
                return Result.failure("product.not.available", "ERR_PROD_01");
            }


            BigDecimal unitPrice = productContract.getActiveProductPrice(itemRequest.productId(), request.restaurantId());


            BigDecimal itemTotalPrice = unitPrice.multiply(BigDecimal.valueOf(itemRequest.quantity()));
            totalOrderPrice = totalOrderPrice.add(itemTotalPrice);

            Product productRef = new Product();
            productRef.setId(itemRequest.productId());

            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(productRef);
            orderItem.setQuantity(itemRequest.quantity());
            orderItem.setUnitPrice(unitPrice);
            orderItem.setTotalPrice(itemTotalPrice);


            order.addItem(orderItem);
        }


        order.setTotalPrice(totalOrderPrice);


        Order savedOrder = orderRepository.save(order);


        return Result.success(mapToResponse(savedOrder), "order.created.successfully");
    }

    private OrderResponse mapToResponse(Order order) {
        return new OrderResponse(
                order.getId(),
                order.getCustomer().getId(),
                order.getRestaurant().getId(),
                order.getStatus().name(),
                order.getTotalPrice(),
                order.getDeliveryAddress()
        );
    }
}
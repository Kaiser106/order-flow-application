package com.orderflow.order.service;

import com.orderflow.auth.service.CustomUserDetails;
import com.orderflow.common.result.Result;
import com.orderflow.customer.contract.CustomerContract;
import com.orderflow.customer.entity.Customer;
import com.orderflow.notification.service.OrderEventService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.orderflow.common.constant.PaginationConstants;
import com.orderflow.common.dto.PageResponse;
import com.orderflow.common.exception.ForbiddenException;
import com.orderflow.common.exception.UnauthorizedException;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderEventService orderEventService;


    private final OrderRepository orderRepository;


    private final CustomerContract customerContract;
    private final RestaurantContract restaurantContract;
    private final ProductContract productContract;


    @Transactional
    public Result<OrderResponse> createOrder(CreateOrderRequest request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof CustomUserDetails userDetails)) {
            return Result.failure("User not authenticated", "ERR_UNAUTHORIZED");
        }

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
    @Transactional(readOnly = true)
    public Result<PageResponse<OrderResponse>> getCustomerOrders(int page, int size) {
        Long currentUserId = getCurrentUserId();
        Long customerId = customerContract.getCustomerIdByUserId(currentUserId);

        int validSize = Math.min(size, PaginationConstants.MAX_PAGE_SIZE);

        Pageable pageable = PageRequest.of(page, validSize, Sort.by(Sort.Direction.DESC, "createdAt"));


        Page<Order> orderPage = orderRepository.findByCustomerId(customerId, pageable);
        Page<OrderResponse> responsePage = orderPage.map(this::mapToResponse);

        return Result.success(PageResponse.of(responsePage));
    }

    @Transactional
    public Result<OrderResponse> cancelOrder(Long orderId) {
        Long currentUserId = getCurrentUserId();
        Long customerId = customerContract.getCustomerIdByUserId(currentUserId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("order.not.found"));

        if (!order.getCustomer().getId().equals(customerId)) {
            throw new ForbiddenException("system.error.forbidden");
        }

        if (order.getStatus() != OrderStatus.PENDING && order.getStatus() != OrderStatus.CONFIRMED) {
            return Result.failure("order.invalid.status", "ERR_ORD_02");
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);
        orderEventService.sendOrderUpdate(orderId, OrderStatus.CANCELLED);
        return Result.success(mapToResponse(savedOrder), "Order cancelled successfully.");
    }


    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("system.error.unauthorized");
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getId();
    }
}
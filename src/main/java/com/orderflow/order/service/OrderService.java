package com.orderflow.order.service;
import com.orderflow.auth.enums.Role;
import com.orderflow.auth.service.CustomUserDetails;
import com.orderflow.common.result.Result;
import com.orderflow.customer.contract.CustomerContract;
import com.orderflow.customer.entity.Customer;
import com.orderflow.courier.contract.CourierContract;
import com.orderflow.courier.entity.Courier;
import com.orderflow.order.notification.OrderEventService;
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
import org.springframework.security.access.prepost.PreAuthorize;
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
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderEventService orderEventService;
    private final OrderRepository orderRepository;
    private final CustomerContract customerContract;
    private final RestaurantContract restaurantContract;
    private final ProductContract productContract;
    private final CourierContract courierContract;

    @Transactional
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<OrderResponse> createOrder(CreateOrderRequest request) {
        UUID currentUserId = getCurrentUserId();
        UUID customerId = customerContract.getCustomerIdByUserId(currentUserId);
        if (!restaurantContract.isRestaurantActive(request.restaurantId())) {
            return Result.failure("restaurant.not.active", "ERR_REST_01");
        }
        Customer customerRef = new Customer();
        customerRef.setId(customerId);
        Restaurant restaurantRef = new Restaurant();
        restaurantRef.setId(request.restaurantId());
        Order order = new Order();
        order.setCustomer(customerRef);
        order.setRestaurant(restaurantRef);
        order.setStatus(OrderStatus.PREPARING);
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

    @Transactional
    @PreAuthorize("hasRole('COURIER')")
    public Result<OrderResponse> updateOrderStatus(UUID orderId, OrderStatus newStatus) {
        UUID currentUserId = getCurrentUserId();
        UUID courierId = courierContract.getCourierIdByUserId(currentUserId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("order.not.found"));

        if (newStatus == OrderStatus.PICKED_UP) {
            if (order.getCourier() != null && !order.getCourier().getId().equals(courierId)) {
                return Result.failure("Bu sipariş başka bir kurye tarafından alınmış.", "ERR_ORD_03");
            }
            Courier courierRef = new Courier();
            courierRef.setId(courierId);
            order.setCourier(courierRef);
        } else {
            if (order.getCourier() == null || !order.getCourier().getId().equals(courierId)) {
                throw new ForbiddenException("Sadece kendi aldığınız siparişleri güncelleyebilirsiniz.");
            }
        }
        order.setStatus(newStatus);
        Order savedOrder = orderRepository.save(order);
        orderEventService.sendOrderUpdate(orderId, newStatus);
        return Result.success(mapToResponse(savedOrder), "Order status updated.");
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('CUSTOMER')")
    public Result<PageResponse<OrderResponse>> getCustomerOrders(int page, int size) {
        UUID currentUserId = getCurrentUserId();
        UUID customerId = customerContract.getCustomerIdByUserId(currentUserId);
        int validSize = Math.min(size, PaginationConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, validSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage = orderRepository.findByCustomerId(customerId, pageable);
        Page<OrderResponse> responsePage = orderPage.map(this::mapToResponse);
        return Result.success(PageResponse.of(responsePage));
    }

    @Transactional
    public Result<OrderResponse> cancelOrder(UUID orderId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("system.error.unauthorized");
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        UUID currentUserId = userDetails.getUser().getId();
        Role userRole = userDetails.getUser().getRole();
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("order.not.found"));

        if (userRole == Role.CUSTOMER) {
            UUID customerId = customerContract.getCustomerIdByUserId(currentUserId);
            if (!order.getCustomer().getId().equals(customerId)) {
                throw new ForbiddenException("system.error.forbidden");
            }
        } else if (userRole == Role.RESTAURANT) {
            UUID restaurantId = restaurantContract.getRestaurantIdByUserId(currentUserId);
            if (!order.getRestaurant().getId().equals(restaurantId)) {
                throw new ForbiddenException("system.error.forbidden");
            }
        } else {
            throw new ForbiddenException("Siparişi sadece müşteri veya restoran iptal edebilir.");
        }
        if (order.getStatus() != OrderStatus.PREPARING) {
            return Result.failure("order.invalid.status", "ERR_ORD_02");
        }
        order.setStatus(OrderStatus.CANCELLED);
        Order savedOrder = orderRepository.save(order);
        orderEventService.sendOrderUpdate(orderId, OrderStatus.CANCELLED);
        return Result.success(mapToResponse(savedOrder), "Order cancelled successfully.");
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

    private UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("system.error.unauthorized");
        }
        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return userDetails.getUser().getId();
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('COURIER')")
    public Result<PageResponse<OrderResponse>> getAvailableOrders(int page, int size) {
        int validSize = Math.min(size, PaginationConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, validSize, Sort.by(Sort.Direction.ASC, "createdAt"));
        Page<Order> orderPage = orderRepository.findByStatusAndCourierIsNull(OrderStatus.PREPARING, pageable);
        Page<OrderResponse> responsePage = orderPage.map(this::mapToResponse);
        return Result.success(PageResponse.of(responsePage));
    }

    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('COURIER')")
    public Result<PageResponse<OrderResponse>> getCourierOrders(int page, int size) {
        UUID currentUserId = getCurrentUserId();
        UUID courierId = courierContract.getCourierIdByUserId(currentUserId);
        int validSize = Math.min(size, PaginationConstants.MAX_PAGE_SIZE);
        Pageable pageable = PageRequest.of(page, validSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Order> orderPage = orderRepository.findByCourierId(courierId, pageable);
        Page<OrderResponse> responsePage = orderPage.map(this::mapToResponse);
        return Result.success(PageResponse.of(responsePage));
    }
}
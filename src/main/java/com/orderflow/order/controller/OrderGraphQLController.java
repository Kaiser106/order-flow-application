package com.orderflow.order.controller;

import com.orderflow.common.dto.PageResponse;
import com.orderflow.common.exception.BusinessException;
import com.orderflow.common.result.Result;
import com.orderflow.order.dto.CreateOrderRequest;
import com.orderflow.order.dto.OrderResponse;
import com.orderflow.order.entity.OrderStatus;
import com.orderflow.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class OrderGraphQLController {

    private final OrderService orderService;

    @MutationMapping
    public OrderResponse createOrder(@Argument CreateOrderRequest input) {
        Result<OrderResponse> result = orderService.createOrder(input);
        if (!result.isSuccess()) {
            throw new BusinessException(result.getMessage(), result.getErrorCode());
        }
        return result.getData();
    }

    @MutationMapping
    public OrderResponse updateOrderStatus(@Argument UUID orderId, @Argument String status) {
        OrderStatus newStatus = OrderStatus.valueOf(status);
        Result<OrderResponse> result = orderService.updateOrderStatus(orderId, newStatus);
        if (!result.isSuccess()) {
            throw new BusinessException(result.getMessage(), result.getErrorCode());
        }
        return result.getData();
    }


    @MutationMapping
    public OrderResponse cancelOrder(@Argument UUID id) {
        Result<OrderResponse> result = orderService.cancelOrder(id);
        if (!result.isSuccess()) {
            throw new BusinessException(result.getMessage(), result.getErrorCode());
        }
        return result.getData();
    }

    @QueryMapping
    public PageResponse<OrderResponse> customerOrders(
            @Argument Integer page,
            @Argument Integer size) {
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? size : 10;
        return orderService.getCustomerOrders(pageNumber, pageSize).getData();
    }

    @QueryMapping
    public PageResponse<OrderResponse> availableOrders(
            @Argument Integer page,
            @Argument Integer size) {
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? size : 10;
        return orderService.getAvailableOrders(pageNumber, pageSize).getData();
    }

    @QueryMapping
    public PageResponse<OrderResponse> courierOrders(
            @Argument Integer page,
            @Argument Integer size) {
        int pageNumber = page != null ? page : 0;
        int pageSize = size != null ? size : 10;
        return orderService.getCourierOrders(pageNumber, pageSize).getData();
    }
}
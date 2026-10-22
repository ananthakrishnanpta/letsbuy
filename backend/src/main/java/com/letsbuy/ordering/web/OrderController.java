package com.letsbuy.ordering.web;

import com.letsbuy.common.api.ApiResponse;
import com.letsbuy.common.api.PageRequests;
import com.letsbuy.common.api.PageResponse;
import com.letsbuy.ordering.service.OrderService;
import com.letsbuy.ordering.web.dto.CheckoutRequest;
import com.letsbuy.ordering.web.dto.OrderResponse;
import com.letsbuy.security.CurrentUser;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class OrderController {

    private final OrderService orders;

    public OrderController(OrderService orders) {
        this.orders = orders;
    }

    @PostMapping("/checkout")
    @ResponseStatus(HttpStatus.CREATED)
    ApiResponse<OrderResponse> checkout(@Valid @RequestBody CheckoutRequest request) {
        return ApiResponse.of(orders.checkout(CurrentUser.id(), request.addressId(), request.paymentMethod()));
    }

    @GetMapping("/orders")
    ApiResponse<PageResponse<OrderResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return ApiResponse.of(orders.list(CurrentUser.id(), PageRequests.clampPage(page), PageRequests.clampSize(size)));
    }

    @GetMapping("/orders/{id}")
    ApiResponse<OrderResponse> get(@PathVariable UUID id) {
        return ApiResponse.of(orders.get(CurrentUser.id(), id));
    }

    @PostMapping("/orders/{id}/cancel")
    ApiResponse<OrderResponse> cancel(@PathVariable UUID id) {
        return ApiResponse.of(orders.cancel(CurrentUser.id(), id));
    }
}

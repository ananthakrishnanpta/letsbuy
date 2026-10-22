package com.letsbuy.ordering.web;

import com.letsbuy.common.api.ApiResponse;
import com.letsbuy.ordering.service.OrderService;
import com.letsbuy.ordering.web.dto.OrderResponse;
import java.util.UUID;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/orders")
public class AdminOrderController {

    private final OrderService orders;

    public AdminOrderController(OrderService orders) {
        this.orders = orders;
    }

    @PostMapping("/{id}/fulfill")
    ApiResponse<OrderResponse> fulfill(@PathVariable UUID id) {
        return ApiResponse.of(orders.fulfill(id));
    }
}

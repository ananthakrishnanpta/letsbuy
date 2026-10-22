package com.letsbuy.cart.web;

import com.letsbuy.cart.service.CartService;
import com.letsbuy.cart.web.dto.CartItemRequest;
import com.letsbuy.cart.web.dto.CartResponse;
import com.letsbuy.cart.web.dto.UpdateQuantityRequest;
import com.letsbuy.common.api.ApiResponse;
import com.letsbuy.security.CurrentUser;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService carts;

    public CartController(CartService carts) {
        this.carts = carts;
    }

    @GetMapping
    ApiResponse<CartResponse> get() {
        return ApiResponse.of(carts.view(CurrentUser.id()));
    }

    @PostMapping("/items")
    ApiResponse<CartResponse> add(@Valid @RequestBody CartItemRequest request) {
        return ApiResponse.of(carts.add(CurrentUser.id(), request.productId(), request.quantity()));
    }

    @PatchMapping("/items/{itemId}")
    ApiResponse<CartResponse> update(@PathVariable UUID itemId, @Valid @RequestBody UpdateQuantityRequest request) {
        return ApiResponse.of(carts.updateQuantity(CurrentUser.id(), itemId, request.quantity()));
    }

    @DeleteMapping("/items/{itemId}")
    ApiResponse<CartResponse> remove(@PathVariable UUID itemId) {
        return ApiResponse.of(carts.remove(CurrentUser.id(), itemId));
    }
}

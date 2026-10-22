package com.letsbuy.cart.web.dto;

import java.util.List;

public record CartResponse(List<CartLineResponse> items, long subtotalMinor, String currency) {
}

package com.letsbuy.cart.web.dto;

import java.util.UUID;

public record CartLineResponse(
        UUID itemId,
        UUID productId,
        String name,
        String slug,
        String imageUrl,
        long unitPriceMinor,
        String currency,
        int quantity,
        int stockQuantity,
        boolean available,
        long lineTotalMinor
) {
}

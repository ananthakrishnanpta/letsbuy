package com.letsbuy.catalog.api;

import java.util.UUID;

public record ProductSnapshot(
        UUID id,
        String sku,
        String name,
        String slug,
        String description,
        long priceMinor,
        String currency,
        int stockQuantity,
        String imageUrl,
        String categorySlug,
        String categoryName,
        boolean purchasable
) {
}

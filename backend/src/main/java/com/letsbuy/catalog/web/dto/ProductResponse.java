package com.letsbuy.catalog.web.dto;

import com.letsbuy.catalog.domain.Product;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String sku,
        String name,
        String slug,
        String description,
        long priceMinor,
        String currency,
        int stockQuantity,
        String status,
        String imageUrl,
        String categorySlug,
        String categoryName
) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getPriceMinor(),
                product.getCurrency(),
                product.getStockQuantity(),
                product.getStatus().name(),
                product.getImageUrl(),
                product.getCategory().getSlug(),
                product.getCategory().getName()
        );
    }
}

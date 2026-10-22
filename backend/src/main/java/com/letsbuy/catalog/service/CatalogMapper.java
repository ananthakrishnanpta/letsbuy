package com.letsbuy.catalog.service;

import com.letsbuy.catalog.api.ProductSnapshot;
import com.letsbuy.catalog.domain.Product;
import com.letsbuy.catalog.domain.ProductStatus;

final class CatalogMapper {

    private CatalogMapper() {
    }

    static ProductSnapshot snapshot(Product product) {
        return new ProductSnapshot(
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getPriceMinor(),
                product.getCurrency(),
                product.getStockQuantity(),
                product.getImageUrl(),
                product.getCategory().getSlug(),
                product.getCategory().getName(),
                product.getStatus() == ProductStatus.ACTIVE && product.getStockQuantity() > 0
        );
    }
}

package com.letsbuy.ordering.web.dto;

import com.letsbuy.ordering.domain.OrderItem;
import java.util.UUID;

public record OrderItemResponse(
        UUID productId,
        String sku,
        String productName,
        long unitPriceMinor,
        int quantity,
        long lineTotalMinor
) {

    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getProductId(),
                item.getSku(),
                item.getProductName(),
                item.getUnitPriceMinor(),
                item.getQuantity(),
                item.getLineTotalMinor()
        );
    }
}

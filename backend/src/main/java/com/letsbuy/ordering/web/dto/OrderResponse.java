package com.letsbuy.ordering.web.dto;

import com.letsbuy.ordering.domain.CustomerOrder;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record OrderResponse(
        UUID id,
        String orderNumber,
        String status,
        String currency,
        long subtotalMinor,
        long shippingMinor,
        long totalMinor,
        String paymentMethod,
        String paymentStatus,
        AddressResponse shippingAddress,
        List<OrderItemResponse> items,
        Instant createdAt
) {

    public static OrderResponse summary(CustomerOrder order, String paymentMethod, String paymentStatus) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus().name(),
                order.getCurrency(),
                order.getSubtotalMinor(),
                order.getShippingMinor(),
                order.getTotalMinor(),
                paymentMethod,
                paymentStatus,
                shipping(order),
                List.of(),
                order.getCreatedAt()
        );
    }

    public static OrderResponse detail(CustomerOrder order, String paymentMethod, String paymentStatus) {
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus().name(),
                order.getCurrency(),
                order.getSubtotalMinor(),
                order.getShippingMinor(),
                order.getTotalMinor(),
                paymentMethod,
                paymentStatus,
                shipping(order),
                order.getItems().stream().map(OrderItemResponse::from).toList(),
                order.getCreatedAt()
        );
    }

    private static AddressResponse shipping(CustomerOrder order) {
        return new AddressResponse(
                null,
                order.getShippingFullName(),
                order.getShippingLine1(),
                order.getShippingLine2(),
                order.getShippingCity(),
                order.getShippingRegion(),
                order.getShippingPostalCode(),
                order.getShippingCountryCode(),
                order.getShippingPhone(),
                false
        );
    }
}

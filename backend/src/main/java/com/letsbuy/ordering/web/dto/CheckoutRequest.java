package com.letsbuy.ordering.web.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CheckoutRequest(@NotNull UUID addressId, @NotNull String paymentMethod) {
}

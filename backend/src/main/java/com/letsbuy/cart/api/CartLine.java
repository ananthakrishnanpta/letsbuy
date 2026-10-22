package com.letsbuy.cart.api;

import java.util.UUID;

public record CartLine(UUID productId, int quantity) {
}

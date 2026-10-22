package com.letsbuy.ordering.domain;

public final class ShippingPolicy {

    public static final long FREE_THRESHOLD_MINOR = 99_900L;
    public static final long FLAT_RATE_MINOR = 4_900L;

    private ShippingPolicy() {
    }

    public static long quote(long subtotalMinor) {
        if (subtotalMinor < 0) {
            throw new IllegalArgumentException("subtotal must be positive");
        }
        return subtotalMinor >= FREE_THRESHOLD_MINOR ? 0 : FLAT_RATE_MINOR;
    }
}

package com.letsbuy.ordering.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ShippingPolicyTest {

    @Test
    void chargesFlatRateBelowThreshold() {
        assertThat(ShippingPolicy.quote(99_899)).isEqualTo(ShippingPolicy.FLAT_RATE_MINOR);
    }

    @Test
    void isFreeAtThreshold() {
        assertThat(ShippingPolicy.quote(ShippingPolicy.FREE_THRESHOLD_MINOR)).isZero();
    }
}

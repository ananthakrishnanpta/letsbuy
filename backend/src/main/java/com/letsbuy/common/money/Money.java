package com.letsbuy.common.money;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class Money {

    private Money() {
    }

    public static long majorToMinor(BigDecimal major) {
        return major.movePointRight(2).setScale(0, RoundingMode.HALF_UP).longValueExact();
    }
}

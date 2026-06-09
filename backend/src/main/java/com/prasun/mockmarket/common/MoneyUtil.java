package com.prasun.mockmarket.common;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class MoneyUtil {
    private MoneyUtil() {
    }

    public static final BigDecimal STARTING_BALANCE = new BigDecimal("100000.00");

    public static BigDecimal money(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP) : value.setScale(2, RoundingMode.HALF_UP);
    }

    public static BigDecimal quantity(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP) : value.setScale(4, RoundingMode.HALF_UP);
    }

    public static BigDecimal percent(BigDecimal value) {
        return value == null ? BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP) : value.setScale(4, RoundingMode.HALF_UP);
    }
}

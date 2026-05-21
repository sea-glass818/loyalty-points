package com.gchoi.loyalty.entity;

import java.math.BigDecimal;

/**
 * Customer tier derived from non-refunded spend over the configured rolling window.
 */
public enum Tier {
    SILVER,
    GOLD,
    PLATINUM;

    /**
     * Determines the loyalty tier for the provided rolling spend amount.
     *
     * @param rollingSpend non-refunded spend in the rolling tier window
     * @return tier matching the configured spend thresholds
     */
    public static Tier fromRollingSpend(BigDecimal rollingSpend) {
        if (rollingSpend.compareTo(new BigDecimal("5000.00")) >= 0) {
            return PLATINUM;
        }
        if (rollingSpend.compareTo(new BigDecimal("1000.00")) >= 0) {
            return GOLD;
        }
        return SILVER;
    }
}

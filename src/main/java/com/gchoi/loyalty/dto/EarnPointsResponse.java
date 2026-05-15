package com.gchoi.loyalty.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Response returned after points are awarded for a purchase.
 */
public record EarnPointsResponse(
        String customerId,
        String purchaseId,
        BigDecimal amount,
        int pointsEarned,
        Instant purchasedAt,
        Instant expiresAt
) {
}

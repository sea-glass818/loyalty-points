package com.gchoi.loyalty.dto;

import java.time.Instant;
import java.util.List;

/**
 * Response returned after a successful reward redemption.
 */
public record RedeemPointsResponse(
        String customerId,
        String rewardId,
        String rewardName,
        int pointsSpent,
        long remainingBalance,
        Instant redeemedAt,
        List<RedemptionAllocationResponse> allocations
) {
}

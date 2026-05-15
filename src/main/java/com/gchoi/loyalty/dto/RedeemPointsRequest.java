package com.gchoi.loyalty.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload used to redeem a reward for a customer.
 */
public record RedeemPointsRequest(
        @NotBlank String customerId,
        @NotBlank String rewardId
) {
}

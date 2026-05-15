package com.gchoi.loyalty.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Request payload used to record a purchase and award points.
 */
public record EarnPointsRequest(
        @NotBlank String customerId,
        @NotBlank String purchaseId,
        @NotNull @DecimalMin(value = "1.00", message = "amount must be at least 1.00") BigDecimal amount,
        Instant purchasedAt
) {
}

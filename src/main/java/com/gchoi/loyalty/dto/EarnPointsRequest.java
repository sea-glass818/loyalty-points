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
        @NotNull @DecimalMin(value = "0.01", message = "amount must be positive") BigDecimal amount,
        Instant purchasedAt
) {
}

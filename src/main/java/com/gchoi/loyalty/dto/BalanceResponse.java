package com.gchoi.loyalty.dto;

import com.gchoi.loyalty.entity.Tier;

/**
 * Response returned when a customer checks their available points and current tier.
 */
public record BalanceResponse(String customerId, long availablePoints, Tier tier) {
}

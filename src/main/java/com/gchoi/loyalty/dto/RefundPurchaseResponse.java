package com.gchoi.loyalty.dto;

import java.time.Instant;

/**
 * Response returned after a purchase refund and point clawback are applied.
 */
public record RefundPurchaseResponse(
        Long refundId,
        String customerId,
        String purchaseId,
        int earnedPoints,
        int removedAvailablePoints,
        int debtPoints,
        int remainingBalance,
        Instant refundedAt
) {
}

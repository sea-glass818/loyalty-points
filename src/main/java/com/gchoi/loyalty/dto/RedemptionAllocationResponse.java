package com.gchoi.loyalty.dto;

import java.time.Instant;

/**
 * Describes how many points were consumed from a single ledger entry during redemption.
 */
public record RedemptionAllocationResponse(
        Long ledgerEntryId,
        int points,
        Instant expiresAt
) {
}

package com.gchoi.loyalty.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload used to refund a previously recorded purchase.
 */
public record RefundPurchaseRequest(@NotBlank String purchaseId) {
}

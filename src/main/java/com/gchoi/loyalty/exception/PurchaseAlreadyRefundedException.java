package com.gchoi.loyalty.exception;

/**
 * Raised when a refund is requested for a purchase that was already refunded.
 */
public class PurchaseAlreadyRefundedException extends RuntimeException {
    /**
     * Creates the exception for the already refunded purchase id.
     *
     * @param purchaseId public purchase id
     */
    public PurchaseAlreadyRefundedException(String purchaseId) {
        super("Purchase already refunded: " + purchaseId);
    }
}

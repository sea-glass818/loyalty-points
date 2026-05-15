package com.gchoi.loyalty.exception;

/**
 * Raised when a requested purchase id does not exist.
 */
public class PurchaseNotFoundException extends RuntimeException {
    /**
     * Creates the exception for the missing purchase id.
     *
     * @param purchaseId public purchase id
     */
    public PurchaseNotFoundException(String purchaseId) {
        super("Purchase not found: " + purchaseId);
    }
}

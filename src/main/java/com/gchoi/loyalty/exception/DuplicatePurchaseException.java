package com.gchoi.loyalty.exception;

/**
 * Raised when a purchase id is submitted more than once.
 */
public class DuplicatePurchaseException extends RuntimeException {
    /**
     * Creates the exception for a duplicate purchase id.
     *
     * @param purchaseId duplicate purchase id
     */
    public DuplicatePurchaseException(String purchaseId) {
        super("Purchase already exists: " + purchaseId);
    }
}

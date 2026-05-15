package com.gchoi.loyalty.exception;

/**
 * Raised when a requested customer id does not exist.
 */
public class CustomerNotFoundException extends RuntimeException {
    /**
     * Creates the exception for the missing customer id.
     *
     * @param customerId public customer id
     */
    public CustomerNotFoundException(String customerId) {
        super("Customer not found: " + customerId);
    }
}

package com.gchoi.loyalty.exception;

/**
 * Raised when a customer attempts to redeem more points than are available.
 */
public class InsufficientPointsException extends RuntimeException {
    /**
     * Creates the exception with available and required point values.
     *
     * @param availablePoints currently available points
     * @param requiredPoints points required for the reward
     */
    public InsufficientPointsException(long availablePoints, int requiredPoints) {
        super("Insufficient points: available=" + availablePoints + ", required=" + requiredPoints);
    }
}

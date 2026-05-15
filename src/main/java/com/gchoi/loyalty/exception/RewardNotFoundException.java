package com.gchoi.loyalty.exception;

/**
 * Raised when a requested reward id does not exist in the catalog.
 */
public class RewardNotFoundException extends RuntimeException {
    /**
     * Creates the exception for the missing reward id.
     *
     * @param rewardId public reward id
     */
    public RewardNotFoundException(String rewardId) {
        super("Reward not found: " + rewardId);
    }
}

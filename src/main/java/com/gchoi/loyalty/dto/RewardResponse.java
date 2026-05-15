package com.gchoi.loyalty.dto;

/**
 * Public representation of a reward in the loyalty catalog.
 */
public record RewardResponse(String rewardId, String name, int pointCost) {
}

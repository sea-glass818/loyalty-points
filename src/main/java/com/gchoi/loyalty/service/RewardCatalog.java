package com.gchoi.loyalty.service;

import com.gchoi.loyalty.dto.RewardResponse;
import com.gchoi.loyalty.exception.RewardNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory reward catalog used by the redemption flow.
 */
@Component
public class RewardCatalog {
    private final Map<String, RewardResponse> rewards = new ConcurrentHashMap<>();

    /**
     * Creates the default reward catalog.
     */
    public RewardCatalog() {
        add(new RewardResponse("free-coffee", "Free Coffee", 50));
        add(new RewardResponse("five-dollar-coupon", "$5 Coupon", 500));
        add(new RewardResponse("ten-dollar-coupon", "$10 Coupon", 1000));
    }

    /**
     * Returns a reward by id or throws when the id is unknown.
     *
     * @param rewardId public reward id
     * @return matching reward
     */
    public RewardResponse getReward(String rewardId) {
        RewardResponse reward = rewards.get(rewardId);
        if (reward == null) {
            throw new RewardNotFoundException(rewardId);
        }
        return reward;
    }

    /**
     * Lists rewards ordered by cost and id.
     *
     * @return available rewards
     */
    public List<RewardResponse> listRewards() {
        return rewards.values().stream()
                .sorted(Comparator.comparingInt(RewardResponse::pointCost).thenComparing(RewardResponse::rewardId))
                .toList();
    }

    /**
     * Adds a reward to the in-memory catalog.
     *
     * @param reward reward to add
     */
    private void add(RewardResponse reward) {
        rewards.put(reward.rewardId(), reward);
    }
}

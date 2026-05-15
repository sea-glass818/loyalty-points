package com.gchoi.loyalty.controller;

import com.gchoi.loyalty.dto.BalanceResponse;
import com.gchoi.loyalty.dto.EarnPointsRequest;
import com.gchoi.loyalty.dto.EarnPointsResponse;
import com.gchoi.loyalty.dto.RedeemPointsRequest;
import com.gchoi.loyalty.dto.RedeemPointsResponse;
import com.gchoi.loyalty.dto.RefundPurchaseRequest;
import com.gchoi.loyalty.dto.RefundPurchaseResponse;
import com.gchoi.loyalty.dto.RewardResponse;
import com.gchoi.loyalty.service.LoyaltyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Versioned REST API for loyalty purchases, balances, rewards, redemptions, and refunds.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Log4j2
public class LoyaltyController {
    private final LoyaltyService loyaltyService;

    /**
     * Records a purchase and awards loyalty points.
     *
     * @param request purchase details
     * @return awarded points and expiry metadata
     */
    @PostMapping("/purchases")
    @ResponseStatus(HttpStatus.CREATED)
    public EarnPointsResponse earnPoints(@Valid @RequestBody EarnPointsRequest request) {
        log.info("Request: earn points customerId={} purchaseId={} amount={}",
                request.customerId(), request.purchaseId(), request.amount());
        EarnPointsResponse response = loyaltyService.earnPoints(request);
        log.info("Response: earned points customerId={} purchaseId={} pointsEarned={}",
                response.customerId(), response.purchaseId(), response.pointsEarned());
        return response;
    }

    /**
     * Returns the customer's available balance and current tier.
     *
     * @param customerId public customer id
     * @return balance response
     */
    @GetMapping("/customers/{customerId}/balance")
    public BalanceResponse getBalance(@PathVariable String customerId) {
        log.info("Request: get balance customerId={}", customerId);
        BalanceResponse response = loyaltyService.getBalance(customerId);
        log.info("Response: balance customerId={} availablePoints={} tier={}",
                response.customerId(), response.availablePoints(), response.tier());
        return response;
    }

    /**
     * Returns the configured reward catalog.
     *
     * @return available rewards
     */
    @GetMapping("/rewards")
    public List<RewardResponse> listRewards() {
        log.info("Request: list rewards");
        List<RewardResponse> response = loyaltyService.listRewards();
        log.info("Response: listed rewards count={}", response.size());
        return response;
    }

    /**
     * Redeems points for a reward using the oldest-expiring points first.
     *
     * @param request redemption request
     * @return redemption result
     */
    @PostMapping("/redemptions")
    @ResponseStatus(HttpStatus.CREATED)
    public RedeemPointsResponse redeemPoints(@Valid @RequestBody RedeemPointsRequest request) {
        log.info("Request: redeem points customerId={} rewardId={}", request.customerId(), request.rewardId());
        RedeemPointsResponse response = loyaltyService.redeemPoints(request);
        log.info("Response: redeemed points customerId={} rewardId={} pointsSpent={} remainingBalance={}",
                response.customerId(), response.rewardId(), response.pointsSpent(), response.remainingBalance());
        return response;
    }

    /**
     * Refunds a purchase and claws back points earned from it.
     *
     * @param request refund request
     * @return refund result
     */
    @PostMapping("/refunds")
    @ResponseStatus(HttpStatus.CREATED)
    public RefundPurchaseResponse refundPurchase(@Valid @RequestBody RefundPurchaseRequest request) {
        log.info("Request: refund purchase purchaseId={}", request.purchaseId());
        RefundPurchaseResponse response = loyaltyService.refundPurchase(request);
        log.info("Response: refunded purchase customerId={} purchaseId={} debtPoints={} remainingBalance={}",
                response.customerId(), response.purchaseId(), response.debtPoints(), response.remainingBalance());
        return response;
    }
}

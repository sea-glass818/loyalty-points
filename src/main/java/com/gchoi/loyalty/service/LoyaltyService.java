package com.gchoi.loyalty.service;

import com.gchoi.loyalty.config.LoyaltyProperties;
import com.gchoi.loyalty.dto.BalanceResponse;
import com.gchoi.loyalty.dto.EarnPointsRequest;
import com.gchoi.loyalty.dto.EarnPointsResponse;
import com.gchoi.loyalty.dto.RedeemPointsRequest;
import com.gchoi.loyalty.dto.RedeemPointsResponse;
import com.gchoi.loyalty.dto.RedemptionAllocationResponse;
import com.gchoi.loyalty.dto.RefundPurchaseRequest;
import com.gchoi.loyalty.dto.RefundPurchaseResponse;
import com.gchoi.loyalty.dto.RewardResponse;
import com.gchoi.loyalty.entity.Customer;
import com.gchoi.loyalty.entity.LedgerEntryType;
import com.gchoi.loyalty.entity.PointsLedger;
import com.gchoi.loyalty.entity.Purchase;
import com.gchoi.loyalty.entity.Refund;
import com.gchoi.loyalty.entity.Tier;
import com.gchoi.loyalty.exception.CustomerNotFoundException;
import com.gchoi.loyalty.exception.DuplicatePurchaseException;
import com.gchoi.loyalty.exception.InsufficientPointsException;
import com.gchoi.loyalty.exception.PurchaseAlreadyRefundedException;
import com.gchoi.loyalty.exception.PurchaseNotFoundException;
import com.gchoi.loyalty.repository.CustomerRepository;
import com.gchoi.loyalty.repository.PointsLedgerRepository;
import com.gchoi.loyalty.repository.PurchaseRepository;
import com.gchoi.loyalty.repository.RefundRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

/**
 * Application service containing loyalty point business rules.
 */
@Log4j2
@Service
@RequiredArgsConstructor
public class LoyaltyService {
    private final CustomerRepository customerRepository;
    private final PurchaseRepository purchaseRepository;
    private final PointsLedgerRepository pointsLedgerRepository;
    private final RefundRepository refundRepository;
    private final RewardCatalog rewardCatalog;
    private final LoyaltyProperties loyaltyProperties;
    private final Clock clock;

    /**
     * Records a purchase and awards one point per whole dollar spent.
     * Purchases under one dollar are valid and earn zero points.
     *
     * @param request purchase details
     * @return earning result
     */
    @Transactional
    public EarnPointsResponse earnPoints(EarnPointsRequest request) {
        if (purchaseRepository.existsByPurchaseId(request.purchaseId())) {
            throw new DuplicatePurchaseException(request.purchaseId());
        }

        Customer customer = customerRepository.findByExternalId(request.customerId())
                .orElseGet(() -> customerRepository.save(Customer.builder()
                        .externalId(request.customerId())
                        .createdAt(Instant.now(clock))
                        .build()));

        Instant purchasedAt = request.purchasedAt() == null ? Instant.now(clock) : request.purchasedAt();
        int pointsEarned = request.amount().setScale(0, RoundingMode.DOWN).intValueExact();

        Purchase purchase = purchaseRepository.save(Purchase.builder()
                .purchaseId(request.purchaseId())
                .customer(customer)
                .amount(request.amount())
                .purchasedAt(purchasedAt)
                .build());

        Instant expiresAt = purchasedAt.atZone(ZoneOffset.UTC)
                .plusMonths(loyaltyProperties.getPointsExpireAfterMonths())
                .toInstant();

        if (pointsEarned > 0) {
            pointsLedgerRepository.save(PointsLedger.builder()
                    .customer(customer)
                    .purchase(purchase)
                    .entryType(LedgerEntryType.EARN)
                    .points(pointsEarned)
                    .remainingPoints(pointsEarned)
                    .earnedAt(purchasedAt)
                    .expiresAt(expiresAt)
                    .build());
        }

        log.info("Awarded {} points to customer {} for purchase {}", pointsEarned, request.customerId(), request.purchaseId());

        return new EarnPointsResponse(
                customer.getExternalId(),
                purchase.getPurchaseId(),
                purchase.getAmount(),
                pointsEarned,
                purchase.getPurchasedAt(),
                expiresAt
        );
    }

    /**
     * Calculates a customer's available points and rolling-spend tier.
     *
     * @param customerId public customer id
     * @return current balance and tier
     */
    @Transactional(readOnly = true)
    public BalanceResponse getBalance(String customerId) {
        Customer customer = customerRepository.findByExternalId(customerId)
                .orElseThrow(() -> new CustomerNotFoundException(customerId));
        Instant now = Instant.now(clock);
        Long points = pointsLedgerRepository.sumAvailablePoints(customer, now);
        BigDecimal rollingSpend = purchaseRepository.sumNonRefundedSpendSince(
                customerId,
                now.atZone(ZoneOffset.UTC)
                        .minusMonths(loyaltyProperties.getPointsExpireAfterMonths())
                        .toInstant()
        );
        return new BalanceResponse(
                customer.getExternalId(),
                Math.toIntExact(points == null ? 0 : points),
                Tier.fromRollingSpend(rollingSpend)
        );
    }

    /**
     * Redeems a reward using unexpired points closest to expiry first.
     *
     * @param request redemption request
     * @return redemption result
     */
    @Transactional
    public RedeemPointsResponse redeemPoints(RedeemPointsRequest request) {
        Customer customer = customerRepository.findByExternalId(request.customerId())
                .orElseThrow(() -> new CustomerNotFoundException(request.customerId()));
        RewardResponse reward = rewardCatalog.getReward(request.rewardId());
        Instant now = Instant.now(clock);

        int availablePoints = Math.toIntExact(pointsLedgerRepository.sumAvailablePoints(customer, now));
        if (availablePoints < reward.pointCost()) {
            throw new InsufficientPointsException(availablePoints, reward.pointCost());
        }

        int pointsToRedeem = reward.pointCost();
        List<RedemptionAllocationResponse> allocations = new ArrayList<>();
        for (PointsLedger ledgerEntry : pointsLedgerRepository.findRedeemableLotsForUpdate(customer, now)) {
            if (pointsToRedeem == 0) {
                break;
            }

            int redeemedFromLot = Math.min(pointsToRedeem, ledgerEntry.getRemainingPoints());
            ledgerEntry.setRemainingPoints(ledgerEntry.getRemainingPoints() - redeemedFromLot);
            pointsToRedeem -= redeemedFromLot;
            allocations.add(new RedemptionAllocationResponse(
                    ledgerEntry.getId(),
                    redeemedFromLot,
                    ledgerEntry.getExpiresAt()
            ));
        }

        if (pointsToRedeem != 0) {
            throw new IllegalStateException("Available balance and redeemable ledger entries are inconsistent");
        }

        log.info("Redeemed reward {} for customer {} using {} points",
                reward.rewardId(), customer.getExternalId(), reward.pointCost());

        return new RedeemPointsResponse(
                customer.getExternalId(),
                reward.rewardId(),
                reward.name(),
                reward.pointCost(),
                availablePoints - reward.pointCost(),
                now,
                allocations
        );
    }

    /**
     * Lists the available reward catalog.
     *
     * @return available rewards
     */
    @Transactional(readOnly = true)
    public List<RewardResponse> listRewards() {
        return rewardCatalog.listRewards();
    }

    /**
     * Refunds a purchase and claws back the points earned from it.
     *
     * @param request refund request
     * @return refund result and post-refund balance
     */
    @Transactional
    public RefundPurchaseResponse refundPurchase(RefundPurchaseRequest request) {
        Purchase purchase = purchaseRepository.findByPurchaseId(request.purchaseId())
                .orElseThrow(() -> new PurchaseNotFoundException(request.purchaseId()));
        if (purchase.isRefunded()) {
            throw new PurchaseAlreadyRefundedException(request.purchaseId());
        }

        Instant now = Instant.now(clock);
        purchase.setRefunded(true);
        purchase.setRefundedAt(now);

        List<PointsLedger> purchaseLedgerEntries = pointsLedgerRepository.findByPurchaseOrderByExpiresAtAscEarnedAtAscIdAsc(purchase);
        int earnedPoints = purchaseLedgerEntries.stream()
                .filter(entry -> entry.getEntryType() == LedgerEntryType.EARN)
                .mapToInt(PointsLedger::getPoints)
                .sum();

        int remainingEarnedPoints = 0;
        int removedAvailablePoints = 0;
        for (PointsLedger entry : purchaseLedgerEntries) {
            if (entry.getEntryType() != LedgerEntryType.EARN) {
                continue;
            }
            int remainingPoints = Math.max(entry.getRemainingPoints(), 0);
            remainingEarnedPoints += remainingPoints;
            if (entry.getExpiresAt().isAfter(now)) {
                removedAvailablePoints += remainingPoints;
            }
            entry.setRemainingPoints(0);
        }

        int debtPoints = earnedPoints - remainingEarnedPoints;
        if (debtPoints > 0) {
            pointsLedgerRepository.save(PointsLedger.builder()
                    .customer(purchase.getCustomer())
                    .purchase(purchase)
                    .entryType(LedgerEntryType.REFUND)
                    .points(-debtPoints)
                    .remainingPoints(-debtPoints)
                    .earnedAt(now)
                    .expiresAt(now.atZone(ZoneOffset.UTC).plusYears(100).toInstant())
                    .build());
        }

        Refund refund = refundRepository.save(Refund.builder()
                .purchase(purchase)
                .customer(purchase.getCustomer())
                .earnedPoints(earnedPoints)
                .removedAvailablePoints(removedAvailablePoints)
                .debtPoints(debtPoints)
                .refundedAt(now)
                .build());

        int remainingBalance = Math.toIntExact(pointsLedgerRepository.sumAvailablePoints(purchase.getCustomer(), now));
        log.info("Refunded purchase {} for customer {}; clawed back {} points",
                purchase.getPurchaseId(), purchase.getCustomer().getExternalId(), earnedPoints);

        return new RefundPurchaseResponse(
                refund.getId(),
                purchase.getCustomer().getExternalId(),
                purchase.getPurchaseId(),
                earnedPoints,
                removedAvailablePoints,
                debtPoints,
                remainingBalance,
                now
        );
    }
}

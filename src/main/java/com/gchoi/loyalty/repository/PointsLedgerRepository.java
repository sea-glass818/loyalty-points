package com.gchoi.loyalty.repository;

import com.gchoi.loyalty.entity.Customer;
import com.gchoi.loyalty.entity.PointsLedger;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

/**
 * Repository for point ledger balance, redemption, and refund queries.
 */
public interface PointsLedgerRepository extends JpaRepository<PointsLedger, Long> {
    /**
     * Sums unexpired available points for a customer.
     *
     * @param customer customer whose balance is requested
     * @param now current timestamp used for expiry filtering
     * @return available point balance
     */
    @Query("""
            select coalesce(sum(l.remainingPoints), 0)
            from PointsLedger l
            where l.customer = :customer
              and l.expiresAt > :now
            """)
    Long sumAvailablePoints(@Param("customer") Customer customer, @Param("now") Instant now);

    /**
     * Locks and returns redeemable ledger rows ordered by closest expiry first.
     *
     * @param customer customer redeeming points
     * @param now current timestamp used for expiry filtering
     * @return redeemable ledger rows in FIFO-by-expiry order
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select l
            from PointsLedger l
            where l.customer = :customer
              and l.expiresAt > :now
              and l.remainingPoints > 0
            order by l.expiresAt asc, l.earnedAt asc, l.id asc
            """)
    List<PointsLedger> findRedeemableLotsForUpdate(@Param("customer") Customer customer, @Param("now") Instant now);

    /**
     * Locks and returns all ledger rows created from a purchase for refund processing.
     *
     * @param purchase purchase being refunded
     * @return ledger rows linked to the purchase
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<PointsLedger> findByPurchaseOrderByExpiresAtAscEarnedAtAscIdAsc(com.gchoi.loyalty.entity.Purchase purchase);
}

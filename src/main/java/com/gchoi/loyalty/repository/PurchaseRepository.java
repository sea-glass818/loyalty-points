package com.gchoi.loyalty.repository;

import com.gchoi.loyalty.entity.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

/**
 * Repository for purchases and rolling-spend queries.
 */
public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    /**
     * Checks whether the given purchase id has already been recorded.
     *
     * @param purchaseId public purchase id
     * @return true when a purchase already exists
     */
    boolean existsByPurchaseId(String purchaseId);

    /**
     * Finds a purchase by its public purchase id.
     *
     * @param purchaseId public purchase id
     * @return matching purchase when present
     */
    Optional<Purchase> findByPurchaseId(String purchaseId);

    /**
     * Sums non-refunded customer spend since the supplied start time.
     *
     * @param customerId public customer id
     * @param start inclusive start of the rolling window
     * @return spend total for tier calculation
     */
    @Query("""
            select coalesce(sum(p.amount), 0)
            from Purchase p
            where p.customer.externalId = :customerId
              and p.refunded = false
              and p.purchasedAt >= :start
            """)
    BigDecimal sumNonRefundedSpendSince(@Param("customerId") String customerId, @Param("start") Instant start);
}

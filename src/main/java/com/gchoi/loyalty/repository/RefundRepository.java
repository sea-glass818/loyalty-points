package com.gchoi.loyalty.repository;

import com.gchoi.loyalty.entity.Refund;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for refund audit records.
 */
public interface RefundRepository extends JpaRepository<Refund, Long> {
}

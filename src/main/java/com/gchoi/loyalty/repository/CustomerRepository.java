package com.gchoi.loyalty.repository;

import com.gchoi.loyalty.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Repository for customer persistence and customer id lookups.
 */
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    /**
     * Finds a customer by the external id supplied by API callers.
     *
     * @param externalId public customer id
     * @return matching customer when present
     */
    Optional<Customer> findByExternalId(String externalId);
}

package com.gchoi.loyalty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot entry point for the loyalty points service.
 */
@SpringBootApplication
public class LoyaltyPointsApplication {
    /**
     * Starts the Spring Boot application.
     *
     * @param args command-line arguments passed to Spring Boot
     */
    public static void main(String[] args) {
        SpringApplication.run(LoyaltyPointsApplication.class, args);
    }
}

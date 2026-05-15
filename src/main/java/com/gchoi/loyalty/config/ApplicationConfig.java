package com.gchoi.loyalty.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Shared infrastructure beans used by the application.
 */
@Configuration
@EnableConfigurationProperties(LoyaltyProperties.class)
public class ApplicationConfig {
    /**
     * Provides a UTC clock so time-based behavior can be centralized and testable.
     *
     * @return system UTC clock
     */
    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}

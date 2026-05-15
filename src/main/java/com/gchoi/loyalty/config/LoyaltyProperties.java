package com.gchoi.loyalty.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Externalized loyalty configuration values.
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "loyalty")
public class LoyaltyProperties {
    private int pointsExpireAfterMonths = 12;
}

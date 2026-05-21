package com.gchoi.loyalty.config;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Min;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Externalized loyalty configuration values.
 */
@Getter
@Setter
@Validated
@ConfigurationProperties(prefix = "loyalty")
public class LoyaltyProperties {
    @Min(1)
    private int pointsExpireAfterMonths;
}

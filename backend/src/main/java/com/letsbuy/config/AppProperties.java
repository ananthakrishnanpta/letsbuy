package com.letsbuy.config;

import java.time.Duration;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app")
public record AppProperties(Jwt jwt, Auth auth, Cors cors, Seed seed) {

    public record Jwt(String issuer, Duration accessTokenTtl, Duration refreshTokenTtl, String secret) {
    }

    public record Auth(
            boolean requireEmailVerification,
            boolean cookieSecure,
            boolean logEmailTokens,
            int maxFailedLogins,
            Duration lockDuration,
            String publicBaseUrl
    ) {
    }

    public record Cors(List<String> allowedOrigins) {
    }

    public record Seed(boolean enabled, String adminEmail, String adminPassword, boolean catalog) {
    }
}

package ru.cvhub.authservice.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public record JwtProperties(
        @Value("${security.jwt.expiration}")
        Duration expiration,
        @Value("${security.jwt.issuer}")
        String issuer,
        @Value("${security.jwt.audience}")
        String audience,
        @Value("${security.jwt.secret}")
        String secret
) {
    public JwtProperties {
        if (expiration == null || expiration.toMillis() <= 0)
            throw new IllegalArgumentException("Expiration must be positive");

        if (issuer == null || issuer.isBlank())
            throw new IllegalArgumentException("Issuer must be provided");

        if (audience == null || audience.isBlank())
            throw new IllegalArgumentException("Audience must be provided");

        if (secret == null || secret.isBlank())
            throw new IllegalArgumentException("Secret must be provided");
    }
}

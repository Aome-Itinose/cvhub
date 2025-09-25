package ru.cvhub.authservice.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;

@Component
public class JwtUtil {
    private final Long expiration = 3600_000L;
    private final String issuer = "cvhub-auth-service";
    private final String audience = "cvhub-gateway";
    private final String secret = "cvhub-secret";

    public String generateToken(String userId, String email, boolean mfaEnabled, List<String> roles) {
        return JWT.create()
                .withSubject(userId)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expiration))
                .withIssuer(issuer)
                .withAudience(audience)
                .withClaim("roles", roles)
                .withClaim("email", email)
                .withClaim("mfa_enabled", mfaEnabled)
                .sign(Algorithm.HMAC256(secret));
    }
}

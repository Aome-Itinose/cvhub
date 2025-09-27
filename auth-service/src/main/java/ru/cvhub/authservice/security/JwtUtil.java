package ru.cvhub.authservice.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final JwtProperties jwtProperties;

    public String generateToken(UUID userId, String email, boolean mfaEnabled, List<String> roles) {
        return JWT.create()
                .withSubject(userId.toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtProperties.expiration().toMillis()))
                .withIssuer(jwtProperties.issuer())
                .withAudience(jwtProperties.audience())
                .withClaim("roles", roles)
                .withClaim("email", email)
                .withClaim("mfa_enabled", mfaEnabled)
                .sign(Algorithm.HMAC256(jwtProperties.secret()));
    }
}

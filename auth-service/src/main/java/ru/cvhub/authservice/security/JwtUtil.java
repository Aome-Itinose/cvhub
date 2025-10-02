package ru.cvhub.authservice.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.cvhub.authservice.util.exception.AccessTokenException;

import java.util.Date;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    private final JwtProperties jwtProperties;

    public String generateToken(JwtUserDetails jwtUserDetails) {
        return JWT.create()
                .withSubject(jwtUserDetails.id().toString())
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + jwtProperties.expiration().toMillis()))
                .withIssuer(jwtProperties.issuer())
                .withAudience(jwtProperties.audience())
                .withClaim("email", jwtUserDetails.email())
                .withClaim("mfa_enabled", jwtUserDetails.mfaEnabled())
                .sign(Algorithm.HMAC256(jwtProperties.secret()));
    }

    public JwtUserDetails parseToken(String token) {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtProperties.secret()))
                    .withIssuer(jwtProperties.issuer())
                    .withAudience(jwtProperties.audience())
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            return new JwtUserDetails(
                    UUID.fromString(jwt.getSubject()),
                    jwt.getClaim("email").asString(),
                    jwt.getClaim("mfa_enabled").asBoolean()
            );
        } catch (TokenExpiredException e) {
            throw AccessTokenException.expiredAccessToken();
        } catch (JWTVerificationException e) {
            throw AccessTokenException.invalidAccessToken();
        }
    }

    public record JwtUserDetails(
            UUID id,
            String email,
            boolean mfaEnabled
    ) {}
}

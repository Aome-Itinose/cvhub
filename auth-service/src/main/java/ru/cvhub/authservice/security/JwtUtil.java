package ru.cvhub.authservice.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.cvhub.authservice.util.exception.AuthenticationException;

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
                .withClaim(UserDetailsFields.EMAIL.key(), jwtUserDetails.email())
                .withClaim(UserDetailsFields.MFA_ENABLED.key(), jwtUserDetails.mfaEnabled())
                .sign(Algorithm.HMAC256(jwtProperties.secret()));
    }

    public JwtUserDetails parseToken(String token) throws AuthenticationException {
        try {
            JWTVerifier verifier = JWT.require(Algorithm.HMAC256(jwtProperties.secret()))
                    .withIssuer(jwtProperties.issuer())
                    .withAudience(jwtProperties.audience())
                    .build();
            DecodedJWT jwt = verifier.verify(token);
            return new JwtUserDetails(
                    UUID.fromString(jwt.getSubject()),
                    jwt.getClaim(UserDetailsFields.EMAIL.key()).asString(),
                    jwt.getClaim(UserDetailsFields.MFA_ENABLED.key()).asBoolean()
            );
        } catch (TokenExpiredException e) {
            throw AuthenticationException.expiredAccessToken();
        } catch (JWTVerificationException e) {
            throw AuthenticationException.invalidAccessToken();
        }
    }

    private enum UserDetailsFields {
        ID("sub"),
        EMAIL("email"),
        MFA_ENABLED("mfa_enabled");

        private final String key;

        UserDetailsFields(String key) {
            this.key = key;
        }

        public String key() {
            return key;
        }
    }

    public record JwtUserDetails(
            UUID id,
            String email,
            boolean mfaEnabled
    ) {}
}

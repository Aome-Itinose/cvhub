package ru.cvhub.authservice.services;

import com.github.f4b6a3.uuid.util.UuidValidator;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.cvhub.authservice.security.JwtUtil;
import ru.cvhub.authservice.services.dto.TokenDto;
import ru.cvhub.authservice.store.entity.Session;
import ru.cvhub.authservice.store.entity.User;
import ru.cvhub.authservice.store.repository.SessionRepository;
import ru.cvhub.authservice.store.repository.UserRepository;
import ru.cvhub.authservice.util.exception.AuthenticationException;
import ru.cvhub.authservice.util.exception.InactiveUserException;
import ru.cvhub.authservice.util.exception.InvalidInputException;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {
    private final SessionRepository sessionRepository;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    @Value("${security.session.ttl}")
    private Duration sessionTtl;

    @Override
    public @NotNull TokenDto createSessionToken(@NotNull User user) {
        Session activeSession = findOrCreateActiveSession(user.id());
        String accessToken = generateAccessToken(user);

        return new TokenDto(accessToken, activeSession.refreshToken().toString());
    }

    @Override
    public @NotNull TokenDto refreshSessionToken(@NotNull TokenDto tokenDto) {
        String refreshTokenString = tokenDto.refreshToken();

        if (!UuidValidator.isValid(refreshTokenString)) throw InvalidInputException.invalidRefreshToken();
        UUID refreshToken = UUID.fromString(refreshTokenString);

        Session session = sessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(InvalidInputException::invalidRefreshToken);
        if (!session.isValid()) throw InvalidInputException.invalidRefreshToken();
        if (session.expiresAt().isBefore(Instant.now())) throw AuthenticationException.expiredRefreshToken();

        UUID userId = session.userId();
        User user = userRepository.findById(userId)
                .orElseThrow(InvalidInputException::invalidRefreshToken);
        if (!user.isActive()) throw InactiveUserException.userInactive();

        String accessToken = generateAccessToken(user);

        return new TokenDto(accessToken, refreshTokenString);
    }

    private @NotNull Session findOrCreateActiveSession(@NotNull UUID userId) {
        return sessionRepository.findByUserId(userId)
                .stream()
                .filter(this::isSessionValid)
                .findFirst()
                .orElseGet(() -> {
                    Session newSession = new Session(userId, sessionTtl.toMillis());
                    return sessionRepository.save(newSession);
                });
    }

    private @NotNull String generateAccessToken(@NotNull User user) {
        return jwtUtil.generateToken(
                new JwtUtil.JwtUserDetails(
                        user.id(),
                        user.email(),
                        false
                )
        );
    }

    private boolean isSessionValid(@NotNull Session session) {
        return session.isValid() && session.expiresAt().isAfter(Instant.now());
    }
}

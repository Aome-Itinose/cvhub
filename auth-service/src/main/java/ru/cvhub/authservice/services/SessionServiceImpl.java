package ru.cvhub.authservice.services;

import com.github.f4b6a3.uuid.util.UuidValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Marker;
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
import ru.cvhub.authservice.util.logging.SessionLog;
import ru.cvhub.authservice.util.logging.UserLog;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static ru.cvhub.authservice.util.logging.LogUtil.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionServiceImpl implements SessionService {
    private static final Marker LOG_MARKER = marker("SESSION", "SERVICE");

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
        Optional<Session> maybeSession = sessionRepository.findByUserId(userId)
                .stream()
                .filter(this::isSessionValid)
                .findFirst();

        if (maybeSession.isPresent()) {
            Session existingSession = maybeSession.get();
            info(LOG_MARKER, "Found existing active session", "session", SessionLog.from(existingSession));
            return existingSession;
        } else {
            Session newSession = new Session(userId, sessionTtl.toMillis());
            newSession = sessionRepository.save(newSession);

            info(LOG_MARKER, "Created new session", "session", SessionLog.from(newSession));

            return newSession;
        }
    }

    private @NotNull String generateAccessToken(@NotNull User user) {
        String newJwt = jwtUtil.generateToken(
                new JwtUtil.JwtUserDetails(
                        user.id(),
                        user.email(),
                        false
                )
        );

        info(LOG_MARKER, "Generated new JWT", Map.of(
                "jwt", hideSensitiveData(newJwt),
                "user", UserLog.from(user)
        ));

        return newJwt;
    }

    private boolean isSessionValid(@NotNull Session session) {
        return session.isValid() && session.expiresAt().isAfter(Instant.now());
    }
}

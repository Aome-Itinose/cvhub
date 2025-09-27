package ru.cvhub.authservice.services;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.cvhub.authservice.security.JwtUtil;
import ru.cvhub.authservice.services.dto.TokenDto;
import ru.cvhub.authservice.store.entity.Role;
import ru.cvhub.authservice.store.entity.Session;
import ru.cvhub.authservice.store.entity.User;
import ru.cvhub.authservice.store.repository.SessionRepository;
import ru.cvhub.authservice.store.repository.UserRepository;
import ru.cvhub.authservice.util.exception.ExpiredRefreshTokenException;
import ru.cvhub.authservice.util.exception.InvalidRefreshTokenException;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
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
        Session activeSession = findOrCreateActiveSession(user.getId());
        String accessToken = generateAccessToken(user);

        return new TokenDto(accessToken, activeSession.getRefreshToken().toString());
    }

    @Override
    public @NotNull TokenDto refreshSessionToken(@NotNull TokenDto tokenDto) {
        String refreshTokenString = tokenDto.refreshToken();

        UUID refreshToken = UUID.fromString(refreshTokenString);
        Session session = sessionRepository.findByRefreshToken(refreshToken)
                .orElseThrow(InvalidRefreshTokenException::new);
        if(!session.getIsValid()) throw new InvalidRefreshTokenException();
        if(session.getExpiresAt().isBefore(Instant.now())) throw new ExpiredRefreshTokenException();

        UUID userId = session.getUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        String accessToken = generateAccessToken(user);

        return new TokenDto(accessToken, refreshTokenString);
    }

    private @NotNull Session findOrCreateActiveSession(@NotNull UUID userId) {
        return sessionRepository.findByUserId(userId)
                .filter(this::isSessionValid)
                .orElseGet(() -> {
                    Session newSession = new Session(userId, sessionTtl.toMillis());
                    return sessionRepository.save(newSession);
                });
    }

    private @NotNull String generateAccessToken(@NotNull User user) {
        List<String> roleNames = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        return jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                false,
                roleNames
        );
    }

    private boolean isSessionValid(@NotNull Session session) {
        return session.getIsValid() && session.getExpiresAt().isAfter(Instant.now());
    }
}

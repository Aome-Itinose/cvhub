package ru.cvhub.authservice.services;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cvhub.authservice.store.entity.Session;
import ru.cvhub.authservice.store.repository.SessionRepository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionService {
    private static final long SESSION_TTL_DAYS = 30;

    private final SessionRepository sessionRepository;

    @Transactional
    public @NotNull Session createSession(@NotNull UUID userId) {
        Optional<Session> maybeSession = sessionRepository.findById(userId);
        if (maybeSession.isPresent()) {
            Session session = maybeSession.get();
            if(session.getIsValid() && session.getExpiresAt().isAfter(Instant.now())){
                return session;
            }else{
                sessionRepository.delete(session);
            }
        }

        var session = new Session(
                userId,
                SESSION_TTL_DAYS * 24 * 60 * 60 * 1000L // 30 days in milliseconds
        );
        return sessionRepository.save(session);
    }
}

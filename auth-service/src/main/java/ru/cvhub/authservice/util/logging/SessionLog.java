package ru.cvhub.authservice.util.logging;

import com.fasterxml.jackson.annotation.JsonFormat;
import ru.cvhub.authservice.store.entity.Session;

import java.time.Instant;
import java.util.UUID;

public record SessionLog(
        UUID id,
        UUID userId,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXX", timezone = "Europe/Moscow")
        Instant createdAt,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXX", timezone = "Europe/Moscow")
        Instant expiresAt,
        Boolean isValid
) implements EntityLog {
    public static SessionLog from(Session session) {
        return new SessionLog(
                session.id(),
                session.userId(),
                session.createdAt(),
                session.expiresAt(),
                session.isValid()
        );
    }

    @Override
    public String getClassName() {
        return Session.class.getName();
    }
}

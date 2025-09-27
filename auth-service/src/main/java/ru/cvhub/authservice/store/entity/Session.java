package ru.cvhub.authservice.store.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.UUID;

@Entity(name = "sessions")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@NoArgsConstructor
public class Session {
    @Id
    @Column(name = "refresh_token", unique = true)
    UUID refreshToken;

    @Column(name = "user_id", nullable = false)
    UUID userId;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    Instant expiresAt;

    @ColumnDefault("true")
    @Column(name = "is_valid", nullable = false)
    Boolean isValid;

    public Session(UUID userId, Long sessionDurationMs) {
        this.refreshToken = UuidCreator.getTimeOrderedEpoch();
        this.userId = userId;
        this.createdAt = Instant.now();
        this.expiresAt = createdAt.plusMillis(sessionDurationMs);
        this.isValid = true;
    }
}

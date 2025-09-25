package ru.cvhub.authservice.store.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.UUID;

@Entity(name = "sessions")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Session {
    @Id
    @Column(name = "id", unique = true)
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id")
    User user;

    @Column(name = "refresh_token", nullable = false, length = Integer.MAX_VALUE)
    String refreshToken;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    Instant expiresAt;

    @ColumnDefault("true")
    @Column(name = "is_valid", nullable = false)
    Boolean isValid = false;
}

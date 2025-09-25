package ru.cvhub.authservice.store.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @Column(unique = true, nullable = false)
    UUID id;

    @Column(name = "email", nullable = false)
    String email;

    @Column(name = "password_hash", nullable = false)
    String passwordHash;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "created_at", nullable = false)
    Instant createdAt;

    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updated_at")
    Instant updatedAt;

    @ColumnDefault("true")
    @Column(name = "is_active", nullable = false)
    Boolean isActive = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    List<Role> roles;

    public User(String email, String passwordHash) {
        this.id = UuidCreator.getTimeOrderedEpoch();
        this.email = email;
        this.passwordHash = passwordHash;
        this.createdAt = Instant.now();
        this.isActive = true;
    }

    public List<Role> getRoles() {
        return roles == null ? Collections.emptyList() : roles;
    }
}

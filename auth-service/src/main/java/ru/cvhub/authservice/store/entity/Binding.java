package ru.cvhub.authservice.store.entity;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity
@Getter
@Table(name = "bindings")
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Binding {
    @Id
    UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Column(name = "resource", nullable = false)
    String resource;

    @Column(name = "permission", nullable = false, length = 50)
    String permission;

    public Binding(
            User user,
            String resource,
            String permission
    ) {
        this.id = UuidCreator.getTimeOrderedEpoch();
        this.user = user;
        this.resource = resource;
        this.permission = permission;
    }
}

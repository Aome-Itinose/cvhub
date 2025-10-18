package ru.cvhub.authservice.store.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Entity
@Getter
@IdClass(Binding.BindingId.class)
@Table(name = "bindings")
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Binding {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    User user;

    @Id
    @Column(name = "resource", nullable = false)
    String resource;

    @Id
    @Column(name = "permission", nullable = false, length = 50)
    String permission;

    @NoArgsConstructor
    @AllArgsConstructor
    public static class BindingId {
        User user;
        String resource;
        String permission;
    }
}

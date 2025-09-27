package ru.cvhub.authservice.store.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.UUID;

@Entity(name = "roles")
@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Role {
    @Id
    @Column(name = "id", nullable = false)
    UUID id;

    @Column(name = "name", nullable = false, length = 50)
    String name;

    @Column(name = "description", length = Integer.MAX_VALUE)
    String description;
}

package ru.cvhub.authservice.store.repository;

import org.jetbrains.annotations.NotNull;
import org.springframework.data.repository.ListCrudRepository;
import ru.cvhub.authservice.store.entity.Session;

import java.util.UUID;

public interface SessionRepository extends ListCrudRepository<Session, UUID> {
    boolean existsByUserId(UUID userId);

    @NotNull Session findByUserId(UUID userId);
}

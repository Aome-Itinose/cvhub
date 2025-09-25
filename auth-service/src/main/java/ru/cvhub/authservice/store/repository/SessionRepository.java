package ru.cvhub.authservice.store.repository;

import org.springframework.data.repository.ListCrudRepository;
import ru.cvhub.authservice.store.entity.Session;

import java.util.UUID;

public interface SessionRepository extends ListCrudRepository<Session, UUID> {
}

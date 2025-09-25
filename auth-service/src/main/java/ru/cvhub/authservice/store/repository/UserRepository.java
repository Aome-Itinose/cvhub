package ru.cvhub.authservice.store.repository;

import org.springframework.data.repository.ListCrudRepository;
import ru.cvhub.authservice.store.entity.User;

import java.util.UUID;

public interface UserRepository extends ListCrudRepository<User, UUID> {
    boolean existsByEmail(String email);
}

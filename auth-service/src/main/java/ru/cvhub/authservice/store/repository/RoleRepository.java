package ru.cvhub.authservice.store.repository;

import org.springframework.data.repository.ListCrudRepository;
import org.springframework.stereotype.Repository;
import ru.cvhub.authservice.store.entity.Role;

import java.util.UUID;

@Repository
public interface RoleRepository extends ListCrudRepository<Role, UUID> {
}

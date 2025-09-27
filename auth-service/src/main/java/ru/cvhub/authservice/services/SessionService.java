package ru.cvhub.authservice.services;

import org.jetbrains.annotations.NotNull;
import ru.cvhub.authservice.services.dto.TokenDto;
import ru.cvhub.authservice.store.entity.User;

public interface SessionService {
    @NotNull TokenDto createSessionToken(@NotNull User user);
    @NotNull TokenDto refreshSessionToken(@NotNull TokenDto tokenDto);
}

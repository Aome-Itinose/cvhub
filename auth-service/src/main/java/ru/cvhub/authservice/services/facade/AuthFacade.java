package ru.cvhub.authservice.services.facade;

import org.jetbrains.annotations.NotNull;
import ru.cvhub.authservice.services.dto.TokenDto;
import ru.cvhub.authservice.services.dto.UserDto;

public interface AuthFacade {
    @NotNull TokenDto register(@NotNull UserDto request);

    @NotNull TokenDto login(@NotNull UserDto request);

    @NotNull TokenDto refresh(@NotNull TokenDto request);
}

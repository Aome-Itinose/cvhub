package ru.cvhub.authservice.services;

import org.jetbrains.annotations.NotNull;
import ru.cvhub.authservice.services.dto.UserDto;
import ru.cvhub.authservice.store.entity.User;

public interface UserService {
    @NotNull User registerUser(@NotNull UserDto userDto);
    @NotNull User loginUser(@NotNull UserDto userDto);
}

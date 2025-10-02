package ru.cvhub.authservice.services.facade;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.cvhub.authservice.services.SessionService;
import ru.cvhub.authservice.services.UserService;
import ru.cvhub.authservice.services.dto.TokenDto;
import ru.cvhub.authservice.services.dto.UserDto;
import ru.cvhub.authservice.store.entity.User;
import ru.cvhub.authservice.util.exception.AuthenticationException;
import ru.cvhub.authservice.util.exception.IncorrectPasswordException;
import ru.cvhub.authservice.util.exception.UserNotFoundException;
import ru.cvhub.authservice.util.validation.UserValidator;

@Service
@RequiredArgsConstructor
public class AuthFacadeImpl implements AuthFacade {
    private final UserService userService;
    private final SessionService sessionService;
    private final UserValidator userValidator;

    @Override
    @Transactional
    public @NotNull TokenDto register(@NotNull UserDto request) {
        userValidator.throwIfInvalidContent(request);
        User createdUser = userService.registerUser(request);
        return sessionService.createSessionToken(createdUser);
    }

    @Override
    @Transactional
    public @NotNull TokenDto login(@NotNull UserDto request) {
        try {
            userValidator.throwIfInvalidContent(request);
            User user = userService.loginUser(request);
            return sessionService.createSessionToken(user);
        } catch (UserNotFoundException | IncorrectPasswordException e) {
            throw AuthenticationException.invalidCredentials();
        }
    }

    @Override
    public @NotNull TokenDto refresh(@NotNull TokenDto request) {
        return sessionService.refreshSessionToken(request);
    }
}

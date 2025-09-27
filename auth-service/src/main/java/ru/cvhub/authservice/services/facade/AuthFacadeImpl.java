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

@Service
@RequiredArgsConstructor
public class AuthFacadeImpl implements AuthFacade {
    private final UserService userService;
    private final SessionService sessionService;

    @Override
    @Transactional
    public @NotNull TokenDto register(@NotNull UserDto request) {
        User createdUser = userService.registerUser(request);
        TokenDto token = sessionService.createSessionToken(createdUser);
        /*
         * mfa etc.
         */
        return token;
    }

    @Override
    @Transactional
    public @NotNull TokenDto login(@NotNull UserDto request) {
        try {
            User user = userService.loginUser(request);
            return sessionService.createSessionToken(user);
        } catch (UserNotFoundException | IncorrectPasswordException e) {
            throw new AuthenticationException(e);
        }
    }

    @Override
    public @NotNull TokenDto refresh(@NotNull TokenDto request) {
        return sessionService.refreshSessionToken(request);
    }
}

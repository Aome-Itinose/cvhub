package ru.cvhub.authservice.services.facade;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cvhub.authservice.services.SessionService;
import ru.cvhub.authservice.services.UserService;
import ru.cvhub.authservice.services.dto.TokenDto;
import ru.cvhub.authservice.services.dto.UserDto;
import ru.cvhub.authservice.store.entity.User;
import ru.cvhub.authservice.util.exception.AuthenticationException;
import ru.cvhub.authservice.util.exception.IncorrectPasswordException;
import ru.cvhub.authservice.util.exception.UserCreationFailedException;
import ru.cvhub.authservice.util.validation.UserValidator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthFacadeTest {
    private static final String TEST_EMAIL = "tester@domain.com";
    private static final String TEST_PASSWORD = "realLy_Str0ng#password(no...)";
    private static final String VALID_ACCESS_TOKEN = "valid_access_token";
    private static final String VALID_REFRESH_TOKEN = "valid_refresh_token";

    UserDto request = new UserDto(TEST_EMAIL, TEST_PASSWORD);
    User user = new User(TEST_EMAIL, TEST_PASSWORD);
    TokenDto token = new TokenDto(VALID_ACCESS_TOKEN, VALID_REFRESH_TOKEN);

    @Mock
    private UserService userService;
    @Mock
    private SessionService sessionService;
    @Mock
    private UserValidator userValidator;

    @InjectMocks
    private AuthFacadeImpl authFacade;

    @Test
    void register_success() {
        when(userService.registerUser(request)).thenReturn(user);
        when(sessionService.createSessionToken(user)).thenReturn(token);

        assertThat(authFacade.register(request)).isEqualTo(token);
    }

    @Test
    void register_fail_unhandledException() {
        when(userService.registerUser(request)).thenThrow(UserCreationFailedException.class);

        assertThrows(UserCreationFailedException.class, () -> authFacade.register(request));
    }

    @Test
    void login_success() {
        when(userService.loginUser(request)).thenReturn(user);
        when(sessionService.createSessionToken(user)).thenReturn(token);

        assertThat(authFacade.login(request)).isEqualTo(token);
    }

    @Test
    void login_fail_handledException() {
        when(userService.loginUser(request)).thenThrow(IncorrectPasswordException.class);

        assertThrows(AuthenticationException.class, () -> authFacade.login(request));
    }

    @Test
    void login_fail_unhandledException() {
        when(userService.loginUser(request)).thenThrow(UserCreationFailedException.class);

        assertThrows(UserCreationFailedException.class, () -> authFacade.login(request));
    }

    @Test
    void refresh_success() {
        when(sessionService.refreshSessionToken(token)).thenReturn(token);

        assertThat(authFacade.refresh(token)).isEqualTo(token);
    }

    @Test
    void refresh_fail() {
        when(sessionService.refreshSessionToken(token)).thenThrow(AuthenticationException.expiredRefreshToken());

        assertThrows(AuthenticationException.class, () -> authFacade.refresh(token));
    }
}
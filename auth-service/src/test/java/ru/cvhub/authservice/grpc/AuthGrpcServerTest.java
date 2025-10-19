package ru.cvhub.authservice.grpc;

import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.cvhub.authservice.AbstractGrpcServerTest;
import ru.cvhub.authservice.store.entity.Session;
import ru.cvhub.authservice.store.entity.User;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.cvhub.authservice.grpc.TestFixtures.*;

@ExtendWith(MockitoExtension.class)
class AuthGrpcServerTest extends AbstractGrpcServerTest {

    @BeforeEach
    void setUp() {
        userRepository().deleteAll();
        sessionRepository().deleteAll();
        userRepository().save(new User(
                TEST_EXISTING_USER_ID,
                TEST_EXISTING_USER_EMAIL,
                passwordEncoder().encode(TEST_VALID_PASSWORD),
                Instant.now(),
                Instant.now(),
                true
        ));
        userRepository().save(new User(
                TEST_EXISTING_ANOTHER_USER_ID,
                TEST_EXISTING_ANOTHER_USER_EMAIL,
                passwordEncoder().encode(TEST_VALID_PASSWORD),
                Instant.now(),
                Instant.now(),
                true
        ));
        userRepository().save(new User(
                TEST_EXISTING_INACTIVE_USER_ID,
                TEST_EXISTING_INACTIVE_USER_EMAIL,
                passwordEncoder().encode(TEST_VALID_PASSWORD),
                Instant.now(),
                Instant.now(),
                false
        ));

        sessionRepository().save(new Session(
                TEST_VALID_REFRESH_TOKEN,
                TEST_EXISTING_USER_ID,
                Instant.now(),
                Instant.now().plusSeconds(5),
                true
        ));
        sessionRepository().save(new Session(
                TEST_EXPIRED_REFRESH_TOKEN,
                TEST_EXISTING_ANOTHER_USER_ID,
                Instant.now().minusSeconds(20),
                Instant.now().minusSeconds(10),
                true
        ));
        sessionRepository().save(new Session(
                TEST_INACTIVE_USERS_REFRESH_TOKEN,
                TEST_EXISTING_INACTIVE_USER_ID,
                Instant.now(),
                Instant.now().plusSeconds(5),
                true
        ));
    }

    @Test
    void register_success() {
        AS.RegisterRequest request = AS.RegisterRequest.newBuilder()
                .setEmail(TEST_VALID_EMAIL)
                .setPassword(TEST_VALID_PASSWORD)
                .build();

        AS.TokenResponse response = authServiceStub().register(request);

        verifyUserByEmail(
                TEST_VALID_EMAIL,
                TEST_VALID_PASSWORD
        );
        User createdUser = getUserByEmail(TEST_VALID_EMAIL);
        verifyRefreshToken(
                response.getRefreshToken(),
                createdUser.id()
        );
        verifyAccessToken(
                response.getAccessToken(),
                createdUser.id(),
                TEST_VALID_EMAIL,
                false
        );
    }

    @Test
    void register_fail_invalidEmailFormat() {
        String invalidEmail = "invalid-email";
        AS.RegisterRequest request = AS.RegisterRequest.newBuilder()
                .setEmail(invalidEmail)
                .setPassword(TEST_VALID_PASSWORD)
                .build();

        verifyException(
                StatusRuntimeException.class,
                () -> authServiceStub().register(request),
                "INVALID_ARGUMENT: Invalid email format"
        );
        verifyEmpty(userRepository()::findByEmail, invalidEmail);
    }

    @Test
    void register_fail_weakPassword() {
        AS.RegisterRequest request = AS.RegisterRequest.newBuilder()
                .setEmail(TEST_VALID_EMAIL)
                .setPassword("weakpass")
                .build();

        verifyException(
                StatusRuntimeException.class,
                () -> authServiceStub().register(request),
                "INVALID_ARGUMENT: Password must be at least 8 characters. Contains at least one uppercase letter, one lowercase letter, one digit and one special character"
        );
        verifyEmpty(userRepository()::findByEmail, TEST_VALID_EMAIL);
    }

    @Test
    void register_fail_emailAlreadyExists() {
        AS.RegisterRequest request = AS.RegisterRequest.newBuilder()
                .setEmail(TEST_EXISTING_USER_EMAIL)
                .setPassword(TEST_VALID_PASSWORD)
                .build();

        verifyException(
                StatusRuntimeException.class,
                () -> authServiceStub().register(request),
                "ALREADY_EXISTS: Email already exists"
        );
        verifyPresent(userRepository()::findByEmail, TEST_EXISTING_USER_EMAIL);
    }

    @Test
    void register_fail_blankEmail() {
        AS.RegisterRequest request = AS.RegisterRequest.newBuilder()
                .setPassword("short")
                .build();

        verifyException(
                StatusRuntimeException.class,
                () -> authServiceStub().register(request),
                "INVALID_ARGUMENT: Email must be provided"
        );
        verifyEmpty(userRepository()::findByEmail, "");
    }


    @Test
    void register_fail_blankPassword() {
        AS.RegisterRequest request = AS.RegisterRequest.newBuilder()
                .setEmail(TEST_VALID_EMAIL)
                .build();

        verifyException(
                StatusRuntimeException.class,
                () -> authServiceStub().register(request),
                "INVALID_ARGUMENT: Password must be provided"
        );
        verifyEmpty(userRepository()::findByEmail, "");
    }

    @Test
    void login_success() {
        AS.LoginRequest request = AS.LoginRequest.newBuilder()
                .setEmail(TEST_EXISTING_USER_EMAIL)
                .setPassword(TEST_VALID_PASSWORD)
                .build();

        AS.TokenResponse response = authServiceStub().login(request);
        verifyRefreshToken(response.getRefreshToken(), TEST_EXISTING_USER_ID);
        verifyAccessToken(
                response.getAccessToken(),
                TEST_EXISTING_USER_ID,
                TEST_EXISTING_USER_EMAIL,
                false
        );
    }

    @Test
    void login_fail_userNotFound() {
        AS.LoginRequest request = AS.LoginRequest.newBuilder()
                .setEmail(TEST_VALID_EMAIL)
                .setPassword(TEST_VALID_PASSWORD)
                .build();

        verifyException(
                StatusRuntimeException.class,
                () -> authServiceStub().login(request),
                "UNAUTHENTICATED: Invalid credentials"
        );
    }

    @Test
    void login_fail_incorrectPassword() {
        AS.LoginRequest request = AS.LoginRequest.newBuilder()
                .setEmail(TEST_EXISTING_USER_EMAIL)
                .setPassword("Incorrect#Password1")
                .build();

        verifyException(
                StatusRuntimeException.class,
                () -> authServiceStub().login(request),
                "UNAUTHENTICATED: Invalid credentials"
        );
    }

    @Test
    void login_fail_inactiveUser() {
        AS.LoginRequest request = AS.LoginRequest.newBuilder()
                .setEmail(TEST_EXISTING_INACTIVE_USER_EMAIL)
                .setPassword(TEST_VALID_PASSWORD)
                .build();

        verifyException(
                StatusRuntimeException.class,
                () -> authServiceStub().login(request),
                "PERMISSION_DENIED: Account is inactive"
        );
    }

    @Test
    void login_fail_blankPassword() {
        AS.LoginRequest request = AS.LoginRequest.newBuilder()
                .setEmail(TEST_EXISTING_USER_EMAIL)
                .build();

        verifyException(
                StatusRuntimeException.class,
                () -> authServiceStub().login(request),
                "INVALID_ARGUMENT: Password must be provided"
        );
    }

    @Test
    void login_fail_blankEmail() {
        AS.LoginRequest request = AS.LoginRequest.newBuilder()
                .setPassword(TEST_VALID_PASSWORD)
                .build();

        verifyException(
                StatusRuntimeException.class,
                () -> authServiceStub().login(request),
                "INVALID_ARGUMENT: Email must be provided"
        );
    }

    @Test
    void login_fail_invalidEmailFormat() {
        AS.LoginRequest request = AS.LoginRequest.newBuilder()
                .setEmail("invalid-email-format")
                .setPassword(TEST_VALID_PASSWORD)
                .build();

        verifyException(
                StatusRuntimeException.class,
                () -> authServiceStub().login(request),
                "INVALID_ARGUMENT: Invalid email format"
        );
    }

    @Test
    void refreshToken_success() {
        AS.RefreshTokenRequest request = AS.RefreshTokenRequest.newBuilder()
                .setRefreshToken(TEST_VALID_REFRESH_TOKEN.toString())
                .build();

        AS.TokenResponse response = authServiceStub().refreshToken(request);
        verifyRefreshToken(response.getRefreshToken(), TEST_EXISTING_USER_ID);
        verifyAccessToken(
                response.getAccessToken(),
                TEST_EXISTING_USER_ID,
                TEST_EXISTING_USER_EMAIL,
            false
        );
    }

    @Test
    void refreshToken_fail_invalidTokenFormat() {
        AS.RefreshTokenRequest request = AS.RefreshTokenRequest.newBuilder()
                .setRefreshToken("invalid-token-format")
                .build();

        verifyException(
                StatusRuntimeException.class,
                () -> authServiceStub().refreshToken(request),
                "INVALID_ARGUMENT: Invalid refresh token format"
        );
    }

    @Test
    void refreshToken_fail_expiredToken() {
        AS.RefreshTokenRequest request = AS.RefreshTokenRequest.newBuilder()
                .setRefreshToken(TEST_EXPIRED_REFRESH_TOKEN.toString())
                .build();

        verifyException(
                StatusRuntimeException.class,
                () -> authServiceStub().refreshToken(request),
                "UNAUTHENTICATED: Refresh token has expired"
        );
    }

    @Test
    void refreshToken_fail_inactiveUser() {
        AS.RefreshTokenRequest request = AS.RefreshTokenRequest.newBuilder()
                .setRefreshToken(TEST_INACTIVE_USERS_REFRESH_TOKEN.toString())
                .build();

        verifyException(
                StatusRuntimeException.class,
                () -> authServiceStub().refreshToken(request),
                "PERMISSION_DENIED: Account is inactive"
        );
    }

    private User getUserByEmail(String email) {
        Optional<User> maybeUser = userRepository().findByEmail(email);
        assertThat(maybeUser).isPresent();
        return maybeUser.get();
    }

}
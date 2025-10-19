package ru.cvhub.authservice.grpc;

import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.cvhub.authservice.AbstractGrpcServerTest;
import ru.cvhub.authservice.security.JwtProperties;
import ru.cvhub.authservice.store.entity.User;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;
import static ru.cvhub.authservice.grpc.TestFixtures.*;

@ExtendWith(MockitoExtension.class)
public class PrivateAuthGrpcServerTest extends AbstractGrpcServerTest {

    @MockitoBean
    private JwtProperties jwtProperties;

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

        when(jwtProperties.expiration()).thenReturn(Duration.parse("PT1H"));
        when(jwtProperties.issuer()).thenReturn("TEST_JWT_ISSUER");
        when(jwtProperties.audience()).thenReturn("TEST_JWT_AUDIENCE");
        when(jwtProperties.secret()).thenReturn("TEST_JWT_SECRET");
    }

    @Test
    void isAuthenticated_success() {
        AS.TokenResponse loginResponse = callAndAssertLogin(TEST_EXISTING_USER_EMAIL, TEST_VALID_PASSWORD);

        PAS.EmptyMessage emptyMessage = PAS.EmptyMessage.newBuilder().build();
        PAS.AuthStatusResponse response = callWithToken(
                privateAuthServiceStub(),
                stub -> stub.isAuthenticated(emptyMessage),
                loginResponse.getAccessToken()
        );
        assertThat(response.getIsAuthenticated()).isTrue();
    }

    @Test
    void isAuthenticated_fail_unauthenticated_expiredAccessToken() {
        when(jwtProperties.expiration()).thenReturn(Duration.parse("PT-1S"));

        AS.TokenResponse loginResponse = callAndAssertLogin(TEST_EXISTING_USER_EMAIL, TEST_VALID_PASSWORD);
        PAS.EmptyMessage emptyMessage = PAS.EmptyMessage.newBuilder().build();
        verifyException(
                StatusRuntimeException.class,
                () -> callWithToken(
                        privateAuthServiceStub(),
                        stub -> stub.isAuthenticated(emptyMessage),
                        loginResponse.getAccessToken()
                ),
                "UNAUTHENTICATED: Access token has expired"
        );
    }

    @Test
    void isAuthenticated_fail_unauthenticated_invalidAccessToken() {
        PAS.EmptyMessage emptyMessage = PAS.EmptyMessage.newBuilder().build();
        verifyException(
                StatusRuntimeException.class,
                () -> callWithToken(
                        privateAuthServiceStub(),
                        stub -> stub.isAuthenticated(emptyMessage),
                        "INVALID_ACCESS_TOKEN"
                ),
                "UNAUTHENTICATED: Invalid access token"
        );
    }

    private AS.TokenResponse callAndAssertLogin(
            String email,
            String password
    ) {
        AS.LoginRequest request = AS.LoginRequest.newBuilder()
                .setEmail(email)
                .setPassword(password).build();
        AS.TokenResponse loginResponse = authServiceStub().login(request);

        assertThat(loginResponse).isNotNull();
        assertThat(loginResponse.getAccessToken()).isNotBlank();

        return loginResponse;
    }

}

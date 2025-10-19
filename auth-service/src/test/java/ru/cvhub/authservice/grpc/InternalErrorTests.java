package ru.cvhub.authservice.grpc;

import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import ru.cvhub.authservice.AbstractGrpcServerTest;
import ru.cvhub.authservice.store.repository.UserRepository;
import ru.cvhub.authservice.util.exception.InternalException;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static ru.cvhub.authservice.grpc.TestFixtures.TEST_VALID_EMAIL;
import static ru.cvhub.authservice.grpc.TestFixtures.TEST_VALID_PASSWORD;

@ExtendWith(MockitoExtension.class)
public class InternalErrorTests extends AbstractGrpcServerTest {
    @MockitoBean
    private UserRepository userRepository;

    @BeforeEach
    public void setUp() {
        when(userRepository.existsByEmail(TEST_VALID_EMAIL)).thenThrow(new InternalException());
        when(userRepository.findByEmail(anyString())).thenThrow(new InternalException());
    }

    @Test
    void register_fail_internalError() {
        AS.RegisterRequest request = AS.RegisterRequest.newBuilder()
                .setEmail(TEST_VALID_EMAIL)
                .setPassword(TEST_VALID_PASSWORD)
                .build();

        verifyException(
                StatusRuntimeException.class,
                () -> authServiceStub().register(request),
                "INTERNAL: Internal server error"
        );
    }

    @Test
    void login_fail_internalError() {
        AS.LoginRequest request = AS.LoginRequest.newBuilder()
                .setEmail(TEST_VALID_EMAIL)
                .setPassword(TEST_VALID_PASSWORD)
                .build();

        verifyException(
                StatusRuntimeException.class,
                () -> authServiceStub().login(request),
                "INTERNAL: Internal server error"
        );
    }
}

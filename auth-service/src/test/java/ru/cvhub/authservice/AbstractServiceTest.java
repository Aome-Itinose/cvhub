package ru.cvhub.authservice;

import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.testing.GrpcCleanupRule;
import lombok.Getter;
import org.junit.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import ru.cvhub.authservice.grpc.AuthServiceGrpc;
import ru.cvhub.authservice.grpc.AuthServiceGrpcImpl;
import ru.cvhub.authservice.grpc.ErrorHandlingInterceptor;
import ru.cvhub.authservice.security.JwtUtil;
import ru.cvhub.authservice.store.entity.Session;
import ru.cvhub.authservice.store.entity.User;
import ru.cvhub.authservice.store.repository.SessionRepository;
import ru.cvhub.authservice.store.repository.UserRepository;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.cvhub.authservice.util.AssertMatcher.assertThatOptional;

@Getter(lombok.AccessLevel.PROTECTED)
@SpringBootTest(classes = AuthServiceApplication.class)
@ActiveProfiles("test")
public abstract class AbstractServiceTest {
    @Rule
    public final GrpcCleanupRule grpcCleanup = new GrpcCleanupRule();
    @Autowired
    private AuthServiceGrpcImpl authServiceGrpc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SessionRepository sessionRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private ErrorHandlingInterceptor errorHandlingInterceptor;
    private AuthServiceGrpc.AuthServiceBlockingStub stub;

    @Autowired
    private ConfigurableApplicationContext context;

    @BeforeEach
    void setUp() throws IOException {
        String serverName = InProcessServerBuilder.generateName();
        grpcCleanup.register(InProcessServerBuilder.forName(serverName)
                .directExecutor()
                .addService(authServiceGrpc())
                .intercept(errorHandlingInterceptor)
                .build()
                .start());

        ManagedChannel channel = grpcCleanup.register(InProcessChannelBuilder.forName(serverName)
                .directExecutor()
                .build());
        stub = AuthServiceGrpc.newBlockingStub(channel);
    }

    protected void verifyAccessToken(
            String accessToken,
            UUID expectedUserId,
            String expectedEmail,
            boolean expectedMfaEnabled
    ) {
        assertThat(accessToken).isNotBlank();

        var userDetails = jwtUtil.parseToken(accessToken);
        assertThat(userDetails.email()).isEqualTo(expectedEmail);
        assertThat(userDetails.id()).isEqualTo(expectedUserId);
        assertThat(userDetails.mfaEnabled()).isEqualTo(expectedMfaEnabled);
    }

    protected void verifyRefreshToken(
            String refreshTokenString,
            UUID expectedUserId
    ) {
        UUID refreshToken = UUID.fromString(refreshTokenString);
        assertThatOptional(sessionRepository.findById(refreshToken))
                .hasPartialMatch(new Session(
                        refreshToken,
                        expectedUserId,
                        null,
                        null,
                        true
                ), "createdAt", "expiresAt");
    }

    protected void verifyUserByEmail(
            String email,
            String password
    ) {
        User userToBeCreated = new User(
                null,
                email,
                "hash",
                null,
                null,
                true
        );

        Optional<User> maybeUser = userRepository.findByEmail(email);
        assertThatOptional(maybeUser)
                .hasPartialMatch(userToBeCreated, "id", "passwordHash", "createdAt", "updatedAt")
                .satisfiesAdditional(assertion -> assertion
                        .extracting(User::passwordHash)
                        .asString()
                        .matches(hash -> passwordEncoder.matches(password, hash))
                );
    }

    protected void verifyException(
            Class<StatusRuntimeException> exceptionClass,
            Runnable executable,
            String expectedMessage
    ) {
        StatusRuntimeException exception = assertThrows(exceptionClass, executable::run);
        assertThat(exception.getMessage()).isEqualTo(expectedMessage);
    }

    protected void verifyException(
            Class<StatusRuntimeException> exceptionClass,
            Runnable executable
    ) {
        assertThrows(exceptionClass, executable::run);
    }

    protected <P, T> void verifyEmpty(
            Function<P, Optional<T>> method,
            P parameter
    ) {
        Optional<T> result = method.apply(parameter);
        assertThat(result).isEmpty();
    }

    protected <P, T> void verifyPresent(
            Function<P, Optional<T>> method,
            P parameter
    ) {
        Optional<T> result = method.apply(parameter);
        assertThat(result).isPresent();
    }
}

package ru.cvhub.authservice;

import io.grpc.ManagedChannel;
import io.grpc.Metadata;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import io.grpc.stub.AbstractStub;
import io.grpc.stub.MetadataUtils;
import lombok.AccessLevel;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import ru.cvhub.authservice.grpc.*;
import ru.cvhub.authservice.grpc.interceptor.ErrorHandlingInterceptor;
import ru.cvhub.authservice.grpc.interceptor.GrpcJwtServerInterceptor;
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

@Getter(AccessLevel.PROTECTED)
@SpringBootTest(classes = AuthServiceApplication.class)
@ActiveProfiles("test")
public abstract class AbstractGrpcServerTest {
    @RegisterExtension
    public final GrpcCleanupExtension grpcCleanup = new GrpcCleanupExtension();

    protected AuthServiceGrpc.AuthServiceBlockingStub authServiceStub;
    protected PrivateAuthServiceGrpc.PrivateAuthServiceBlockingStub privateAuthServiceStub;

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
    @Autowired
    private ConfigurableApplicationContext context;
    @Autowired
    private PrivateAuthServiceGrpcImpl privateAuthServiceGrpc;
    @Autowired
    private GrpcJwtServerInterceptor grpcJwtServerInterceptor;

    @BeforeEach
    void setUp() throws IOException {
        String authServiceName = InProcessServerBuilder.generateName();
        grpcCleanup.register(
                InProcessServerBuilder.forName(authServiceName)
                        .directExecutor()
                        .addService(authServiceGrpc)
                        .intercept(errorHandlingInterceptor)
                        .build()
                        .start());

        String privateAuthServiceName = InProcessServerBuilder.generateName();
        grpcCleanup.register(
                InProcessServerBuilder.forName(privateAuthServiceName)
                        .directExecutor()
                        .addService(privateAuthServiceGrpc)
                        .intercept(errorHandlingInterceptor)
                        .intercept(grpcJwtServerInterceptor)
                        .build()
                        .start()
        );

        ManagedChannel authServiceChannel = grpcCleanup.register(InProcessChannelBuilder.forName(authServiceName)
                .directExecutor()
                .build());
        ManagedChannel privateAuthServiceChannel = grpcCleanup.register(InProcessChannelBuilder.forName(privateAuthServiceName)
                .directExecutor()
                .build());

        authServiceStub = AuthServiceGrpc.newBlockingStub(authServiceChannel);
        privateAuthServiceStub = PrivateAuthServiceGrpc.newBlockingStub(privateAuthServiceChannel);
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
        assertThatOptional(sessionRepository.findByRefreshToken(refreshToken))
                .hasPartialMatch(new Session(
                        null,
                        refreshToken,
                        expectedUserId,
                        null,
                        null,
                        true
                ), "id", "createdAt", "expiresAt");
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

    protected <StubT extends AbstractStub<StubT>, RespT> RespT callWithToken(
            StubT stub,
            Function<StubT, RespT> grpcCall,
            String accessToken
    ) {
        Metadata headers = new Metadata();
        Metadata.Key<String> authKey = Metadata.Key.of("Authorization", Metadata.ASCII_STRING_MARSHALLER);
        headers.put(authKey, "Bearer " + accessToken);

        StubT stubWithHeaders = stub.withInterceptors(MetadataUtils.newAttachHeadersInterceptor(headers));
        return grpcCall.apply(stubWithHeaders);
    }
}

package ru.cvhub.authservice.config;

import io.grpc.Status;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.cvhub.authservice.grpc.AbstractGrpcErrorHandler;
import ru.cvhub.authservice.util.exception.*;

@Configuration
public class ErrorHandlerConfig {

    @Bean
    public AbstractGrpcErrorHandler<EmailAlreadyExistsException> emailAlreadyExistsExceptionHandler() {
        return new AbstractGrpcErrorHandler<>(EmailAlreadyExistsException.class) {
            @Override
            public Status handle(EmailAlreadyExistsException exception) {
                return Status
                        .ALREADY_EXISTS
                        .withDescription(exception.getMessage());
            }
        };
    }

    @Bean
    public AbstractGrpcErrorHandler<InvalidInputException> invalidInputExceptionHandler() {
        return new AbstractGrpcErrorHandler<>(InvalidInputException.class) {
            @Override
            public Status handle(InvalidInputException exception) {
                return Status
                        .INVALID_ARGUMENT
                        .withDescription(exception.getMessage());
            }
        };
    }


    @Bean
    public AbstractGrpcErrorHandler<AuthenticationException> authenticationExceptionHandler() {
        return new AbstractGrpcErrorHandler<>(AuthenticationException.class) {
            @Override
            public Status handle(AuthenticationException exception) {
                return Status
                        .UNAUTHENTICATED
                        .withDescription(exception.getMessage());
            }
        };
    }

    @Bean
    public AbstractGrpcErrorHandler<InactiveUserException> inactiveUserExceptionHandler() {
        return new AbstractGrpcErrorHandler<>(InactiveUserException.class) {
            @Override
            public Status handle(InactiveUserException exception) {
                return Status
                        .PERMISSION_DENIED
                        .withDescription(exception.getMessage());
            }
        };
    }

    @Bean
    public AbstractGrpcErrorHandler<ExpiredRefreshTokenException> expiredRefreshTokenExceptionHandler() {
        return new AbstractGrpcErrorHandler<>(ExpiredRefreshTokenException.class) {
            @Override
            public Status handle(ExpiredRefreshTokenException exception) {
                return Status
                        .UNAUTHENTICATED
                        .withDescription(exception.getMessage());
            }
        };
    }

    @Bean
    public AbstractGrpcErrorHandler<RuntimeException> otherExceptionHandler() {
        return new AbstractGrpcErrorHandler<>(RuntimeException.class) {
            @Override
            public Status handle(RuntimeException exception) {
                return Status
                        .INTERNAL
                        .withDescription("Internal server error");
            }
        };
    }
}

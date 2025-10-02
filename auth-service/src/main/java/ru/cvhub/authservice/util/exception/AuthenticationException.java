package ru.cvhub.authservice.util.exception;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }

    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("Invalid credentials");
    }
}

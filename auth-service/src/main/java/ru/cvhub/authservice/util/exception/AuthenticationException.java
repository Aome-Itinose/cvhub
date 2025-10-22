package ru.cvhub.authservice.util.exception;

public final class AuthenticationException extends RuntimeException {
    private AuthenticationException(String message) {
        super(message);
    }

    public static AuthenticationException invalidCredentials() {
        return new AuthenticationException("Invalid credentials");
    }

    public static AuthenticationException invalidAccessToken() {
        return new AuthenticationException("Invalid access token");
    }

    public static AuthenticationException expiredAccessToken() {
        return new AuthenticationException("Access token has expired");
    }

    public static AuthenticationException expiredRefreshToken() {
        return new AuthenticationException("Refresh token has expired");
    }
}

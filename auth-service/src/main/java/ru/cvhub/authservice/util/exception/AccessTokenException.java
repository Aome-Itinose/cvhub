package ru.cvhub.authservice.util.exception;

public class AccessTokenException extends RuntimeException {
    protected AccessTokenException(String message) {
        super(message);
    }

    public static AccessTokenException invalidAccessToken() {
        return new AccessTokenException("Invalid access token");
    }

    public static AccessTokenException expiredAccessToken() {
        return new AccessTokenException("Access token has expired");
    }
}

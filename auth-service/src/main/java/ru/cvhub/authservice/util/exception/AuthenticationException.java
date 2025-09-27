package ru.cvhub.authservice.util.exception;

public class AuthenticationException extends RuntimeException {
    public AuthenticationException(String message) {
        super(message);
    }
    public AuthenticationException(Throwable cause) {
        super(cause.getMessage(), cause);
    }
}

package ru.cvhub.authservice.util.exception;

public final class UserCreationFailedException extends RuntimeException {
    public UserCreationFailedException(String message) {
        super(message);
    }
}

package ru.cvhub.authservice.util.exception;

public final class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("User not found");
    }
}

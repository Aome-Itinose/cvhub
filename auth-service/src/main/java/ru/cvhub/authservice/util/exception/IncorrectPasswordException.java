package ru.cvhub.authservice.util.exception;

public final class IncorrectPasswordException extends RuntimeException {
    public IncorrectPasswordException() {
        super("Incorrect password");
    }
}

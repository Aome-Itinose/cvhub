package ru.cvhub.authservice.util.exception;

public final class InternalException extends RuntimeException {
    public InternalException() {
        super("Internal server error");
    }
}

package ru.cvhub.authservice.util.exception;

public abstract class PreconditionException extends RuntimeException {
    public PreconditionException(String message) {
        super(message);
    }
    public PreconditionException(String message, Throwable cause) {
        super(message, cause);
    }
}

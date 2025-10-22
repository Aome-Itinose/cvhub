package ru.cvhub.authservice.util.exception;

public abstract class PreconditionException extends RuntimeException {
    protected PreconditionException(String message) {
        super(message);
    }
}

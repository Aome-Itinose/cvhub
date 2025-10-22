package ru.cvhub.authservice.util.exception;

public final class EmailAlreadyExistsException extends PreconditionException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}

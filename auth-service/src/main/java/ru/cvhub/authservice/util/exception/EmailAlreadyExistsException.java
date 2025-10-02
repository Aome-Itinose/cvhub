package ru.cvhub.authservice.util.exception;

public class EmailAlreadyExistsException extends PreconditionException {
    public EmailAlreadyExistsException(String message) {
        super(message);
    }
}
